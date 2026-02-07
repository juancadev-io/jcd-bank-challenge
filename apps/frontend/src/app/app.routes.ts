import { Routes } from '@angular/router';
import { CustomersPage } from './pages/customers/customers';
import { AccountsPage } from './pages/accounts/accounts';

export const routes: Routes = [
  { path: '', redirectTo: 'customers', pathMatch: 'full' },
  { path: 'customers', component: CustomersPage },
  { path: 'accounts', component: AccountsPage },
];
