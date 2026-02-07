import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CustomerService } from './customer.service';
import { Customer } from '../models/customer.model';

describe('CustomerService', () => {
  let service: CustomerService;
  let httpTesting: HttpTestingController;

  const mockCustomer: Customer = {
    id: 1,
    documentType: 'CC',
    documentNumber: '123456',
    fullName: 'Juan Perez',
    email: 'juan@test.com',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CustomerService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll should return customers', () => {
    service.getAll().subscribe((customers) => {
      expect(customers).toEqual([mockCustomer]);
    });

    const req = httpTesting.expectOne('/api/customers');
    expect(req.request.method).toBe('GET');
    req.flush([mockCustomer]);
  });

  it('getById should return a customer', () => {
    service.getById(1).subscribe((customer) => {
      expect(customer).toEqual(mockCustomer);
    });

    const req = httpTesting.expectOne('/api/customers/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockCustomer);
  });

  it('create should post and return customer', () => {
    const createData = {
      documentType: 'CC',
      documentNumber: '123456',
      fullName: 'Juan Perez',
      email: 'juan@test.com',
    };

    service.create(createData).subscribe((customer) => {
      expect(customer).toEqual(mockCustomer);
    });

    const req = httpTesting.expectOne('/api/customers');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(createData);
    req.flush(mockCustomer);
  });
});
