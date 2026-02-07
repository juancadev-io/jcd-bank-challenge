export interface Account {
  id: number;
  customerId: number;
  accountNumber: string;
  status: string;
  balance: number;
  createdAt: string;
  updatedAt: string;
}

export interface AccountCreate {
  customerId: number;
}
