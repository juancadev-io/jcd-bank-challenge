import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AccountService } from './account.service';
import { Account } from '../models/account.model';

describe('AccountService', () => {
  let service: AccountService;
  let httpTesting: HttpTestingController;

  const mockAccount: Account = {
    id: 1,
    customerId: 1,
    accountNumber: 'ACC-1234567890-1234',
    status: 'ACTIVE',
    balance: 0,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AccountService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll should return accounts', () => {
    service.getAll().subscribe((accounts) => {
      expect(accounts).toEqual([mockAccount]);
    });

    const req = httpTesting.expectOne('/api/accounts');
    expect(req.request.method).toBe('GET');
    req.flush([mockAccount]);
  });

  it('getByCustomerId should return accounts', () => {
    service.getByCustomerId(1).subscribe((accounts) => {
      expect(accounts).toEqual([mockAccount]);
    });

    const req = httpTesting.expectOne('/api/accounts?customerId=1');
    expect(req.request.method).toBe('GET');
    req.flush([mockAccount]);
  });

  it('create should post and return account', () => {
    service.create({ customerId: 1 }).subscribe((account) => {
      expect(account).toEqual(mockAccount);
    });

    const req = httpTesting.expectOne('/api/accounts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ customerId: 1 });
    req.flush(mockAccount);
  });

  it('updateStatus should patch and return account', () => {
    const updated = { ...mockAccount, status: 'INACTIVE' };

    service.updateStatus(1, 'INACTIVE').subscribe((account) => {
      expect(account).toEqual(updated);
    });

    const req = httpTesting.expectOne('/api/accounts/1/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'INACTIVE' });
    req.flush(updated);
  });
});
