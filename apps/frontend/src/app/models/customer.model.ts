export interface Customer {
  id: number;
  documentType: string;
  documentNumber: string;
  fullName: string;
  email: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerCreate {
  documentType: string;
  documentNumber: string;
  fullName: string;
  email: string;
}
