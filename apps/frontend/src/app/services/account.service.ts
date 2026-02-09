import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account, AccountCreate, TransactionRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/accounts';

  getAll(): Observable<Account[]> {
    return this.http.get<Account[]>(this.apiUrl);
  }

  getByCustomerId(customerId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}?customerId=${customerId}`);
  }

  create(data: AccountCreate): Observable<Account> {
    return this.http.post<Account>(this.apiUrl, data);
  }

  updateStatus(id: number, status: string): Observable<Account> {
    return this.http.patch<Account>(`${this.apiUrl}/${id}/status`, { status });
  }

  transaction(id: number, data: TransactionRequest): Observable<Account> {
    return this.http.post<Account>(`${this.apiUrl}/${id}/transaction`, data);
  }
}
