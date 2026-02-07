import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { AccountService } from '../../services/account.service';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-customers',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class CustomersPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly accountService = inject(AccountService);

  customers = signal<Customer[]>([]);
  loading = signal(false);
  message = signal('');
  messageType = signal<'success' | 'error'>('success');

  form = this.fb.group({
    documentType: ['CC', [Validators.required]],
    documentNumber: ['', [Validators.required]],
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    createAccount: [false],
  });

  ngOnInit() {
    this.loadCustomers();
  }

  loadCustomers() {
    this.loading.set(true);
    this.customerService.getAll().subscribe({
      next: (data) => {
        this.customers.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.showMessage('Error al cargar clientes', 'error');
        this.loading.set(false);
      },
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    const { createAccount, ...customerData } = this.form.getRawValue();
    this.loading.set(true);

    this.customerService.create(customerData as any).subscribe({
      next: (customer) => {
        if (createAccount) {
          this.accountService.create({ customerId: customer.id }).subscribe({
            next: () => {
              this.showMessage('Cliente y cuenta creados exitosamente', 'success');
              this.resetAndReload();
            },
            error: (err) => {
              this.showMessage(
                'Cliente creado, pero error al crear cuenta: ' + (err.error?.message || 'Error desconocido'),
                'error',
              );
              this.resetAndReload();
            },
          });
        } else {
          this.showMessage('Cliente creado exitosamente', 'success');
          this.resetAndReload();
        }
      },
      error: (err) => {
        this.showMessage(err.error?.message || 'Error al crear cliente', 'error');
        this.loading.set(false);
      },
    });
  }

  private resetAndReload() {
    this.form.reset({ documentType: 'CC', createAccount: false });
    this.loadCustomers();
  }

  private showMessage(msg: string, type: 'success' | 'error') {
    this.message.set(msg);
    this.messageType.set(type);
    setTimeout(() => this.message.set(''), 4000);
  }
}
