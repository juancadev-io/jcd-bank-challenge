import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AccountsPage } from './accounts';
import { CustomerService } from '../../services/customer.service';
import { AccountService } from '../../services/account.service';
import { Customer } from '../../models/customer.model';
import { Account } from '../../models/account.model';

describe('AccountsPage', () => {
  let mockCustomerService: any;
  let mockAccountService: any;

  const mockCustomer: Customer = {
    id: 1,
    documentType: 'CC',
    documentNumber: '123456',
    fullName: 'Juan Perez',
    email: 'juan@test.com',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
  };

  const mockAccount: Account = {
    id: 1,
    customerId: 1,
    accountNumber: 'ACC-123',
    status: 'ACTIVE',
    balance: 0,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
  };

  beforeEach(async () => {
    mockCustomerService = {
      getAll: vi.fn().mockReturnValue(of([mockCustomer])),
    };
    mockAccountService = {
      getAll: vi.fn().mockReturnValue(of([mockAccount])),
      create: vi.fn().mockReturnValue(of(mockAccount)),
      updateStatus: vi.fn().mockReturnValue(of({ ...mockAccount, status: 'INACTIVE' })),
    };

    await TestBed.configureTestingModule({
      imports: [AccountsPage],
      providers: [
        { provide: CustomerService, useValue: mockCustomerService },
        { provide: AccountService, useValue: mockAccountService },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load data on init', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    expect(mockCustomerService.getAll).toHaveBeenCalled();
    expect(mockAccountService.getAll).toHaveBeenCalled();
    expect(fixture.componentInstance.rows().length).toBe(1);
    expect(fixture.componentInstance.rows()[0].account).toEqual(mockAccount);
  });

  it('should map customer without account as null', () => {
    mockAccountService.getAll.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    expect(fixture.componentInstance.rows()[0].account).toBeNull();
  });

  it('should handle customer load error', () => {
    mockCustomerService.getAll.mockReturnValue(throwError(() => new Error('fail')));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    expect(fixture.componentInstance.messageType()).toBe('error');
  });

  it('should handle account load error', () => {
    mockAccountService.getAll.mockReturnValue(throwError(() => new Error('fail')));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    expect(fixture.componentInstance.messageType()).toBe('error');
  });

  it('should create account', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.createAccount(1);

    expect(mockAccountService.create).toHaveBeenCalledWith({ customerId: 1 });
  });

  it('should handle create account error', () => {
    mockAccountService.create.mockReturnValue(
      throwError(() => ({ error: { message: 'Already has account' } })),
    );

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.createAccount(1);

    expect(fixture.componentInstance.messageType()).toBe('error');
  });

  it('should toggle status from ACTIVE to INACTIVE', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.toggleStatus(mockAccount);

    expect(mockAccountService.updateStatus).toHaveBeenCalledWith(1, 'INACTIVE');
  });

  it('should toggle status from INACTIVE to ACTIVE', () => {
    const inactiveAccount = { ...mockAccount, status: 'INACTIVE' };

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.toggleStatus(inactiveAccount);

    expect(mockAccountService.updateStatus).toHaveBeenCalledWith(1, 'ACTIVE');
  });

  it('should handle toggle status error', () => {
    mockAccountService.updateStatus.mockReturnValue(
      throwError(() => ({ error: { message: 'Error' } })),
    );

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.toggleStatus(mockAccount);

    expect(fixture.componentInstance.messageType()).toBe('error');
  });
});
