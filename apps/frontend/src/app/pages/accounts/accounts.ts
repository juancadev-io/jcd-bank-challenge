import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { CustomerService } from '../../services/customer.service';
import { AccountService } from '../../services/account.service';
import { Customer } from '../../models/customer.model';
import { Account } from '../../models/account.model';

interface CustomerWithAccount {
  customer: Customer;
  account: Account | null;
}

@Component({
  selector: 'app-accounts',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './accounts.html',
  styleUrl: './accounts.css',
})
export class AccountsPage implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly accountService = inject(AccountService);

  rows = signal<CustomerWithAccount[]>([]);
  loading = signal(false);
  message = signal('');
  messageType = signal<'success' | 'error'>('success');

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    this.customerService.getAll().subscribe({
      next: (customers) => {
        this.accountService.getAll().subscribe({
          next: (accounts) => {
            const accountMap = new Map<number, Account>();
            for (const acc of accounts) {
              accountMap.set(acc.customerId, acc);
            }
            this.rows.set(
              customers.map((c) => ({
                customer: c,
                account: accountMap.get(c.id) ?? null,
              })),
            );
            this.loading.set(false);
          },
          error: () => {
            this.showMessage('Error al cargar cuentas', 'error');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.showMessage('Error al cargar clientes', 'error');
        this.loading.set(false);
      },
    });
  }

  createAccount(customerId: number) {
    this.loading.set(true);
    this.accountService.create({ customerId }).subscribe({
      next: () => {
        this.showMessage('Cuenta creada exitosamente', 'success');
        this.loadData();
      },
      error: (err) => {
        this.showMessage(err.error?.message || 'Error al crear cuenta', 'error');
        this.loading.set(false);
      },
    });
  }

  toggleStatus(account: Account) {
    const newStatus = account.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.accountService.updateStatus(account.id, newStatus).subscribe({
      next: () => {
        this.showMessage(
          `Cuenta ${newStatus === 'ACTIVE' ? 'activada' : 'bloqueada'} exitosamente`,
          'success',
        );
        this.loadData();
      },
      error: (err) => {
        this.showMessage(err.error?.message || 'Error al actualizar estado', 'error');
      },
    });
  }

  private showMessage(msg: string, type: 'success' | 'error') {
    this.message.set(msg);
    this.messageType.set(type);
    setTimeout(() => this.message.set(''), 4000);
  }
}
