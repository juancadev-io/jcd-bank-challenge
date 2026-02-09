import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  imports: [DatePipe, DecimalPipe, FormsModule],
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

  transactionAccountId = signal<number | null>(null);
  transactionType = signal<'DEPOSIT' | 'WITHDRAWAL'>('DEPOSIT');
  transactionAmount = signal<number | null>(null);

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

  openTransaction(accountId: number, type: 'DEPOSIT' | 'WITHDRAWAL') {
    this.transactionAccountId.set(accountId);
    this.transactionType.set(type);
    this.transactionAmount.set(null);
  }

  cancelTransaction() {
    this.transactionAccountId.set(null);
    this.transactionAmount.set(null);
  }

  submitTransaction() {
    const accountId = this.transactionAccountId();
    const amount = this.transactionAmount();
    if (!accountId || !amount || amount <= 0) return;

    this.loading.set(true);
    this.accountService
      .transaction(accountId, { type: this.transactionType(), amount })
      .subscribe({
        next: () => {
          this.showMessage(
            this.transactionType() === 'DEPOSIT'
              ? 'Depósito realizado exitosamente'
              : 'Retiro realizado exitosamente',
            'success',
          );
          this.cancelTransaction();
          this.loadData();
        },
        error: (err) => {
          this.showMessage(err.error?.message || 'Error al realizar la transacción', 'error');
          this.loading.set(false);
        },
      });
  }

  private showMessage(msg: string, type: 'success' | 'error') {
    this.message.set(msg);
    this.messageType.set(type);
    setTimeout(() => this.message.set(''), 4000);
  }
}
