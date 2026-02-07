import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CustomersPage } from './customers';
import { CustomerService } from '../../services/customer.service';
import { AccountService } from '../../services/account.service';
import { Customer } from '../../models/customer.model';

describe('CustomersPage', () => {
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

  beforeEach(async () => {
    mockCustomerService = {
      getAll: vi.fn().mockReturnValue(of([mockCustomer])),
      create: vi.fn().mockReturnValue(of(mockCustomer)),
    };
    mockAccountService = {
      create: vi.fn().mockReturnValue(of({})),
    };

    await TestBed.configureTestingModule({
      imports: [CustomersPage],
      providers: [
        { provide: CustomerService, useValue: mockCustomerService },
        { provide: AccountService, useValue: mockAccountService },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load customers on init', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    expect(mockCustomerService.getAll).toHaveBeenCalled();
    expect(fixture.componentInstance.customers()).toEqual([mockCustomer]);
  });

  it('should handle load error', () => {
    mockCustomerService.getAll.mockReturnValue(throwError(() => new Error('fail')));

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    expect(fixture.componentInstance.messageType()).toBe('error');
  });

  it('should submit and create customer without account', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.setValue({
      documentType: 'CC',
      documentNumber: '999',
      fullName: 'Test',
      email: 'test@test.com',
      createAccount: false,
    });

    component.onSubmit();

    expect(mockCustomerService.create).toHaveBeenCalled();
    expect(mockAccountService.create).not.toHaveBeenCalled();
  });

  it('should submit and create customer with account', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.setValue({
      documentType: 'CC',
      documentNumber: '999',
      fullName: 'Test',
      email: 'test@test.com',
      createAccount: true,
    });

    component.onSubmit();

    expect(mockCustomerService.create).toHaveBeenCalled();
    expect(mockAccountService.create).toHaveBeenCalledWith({ customerId: 1 });
  });

  it('should handle create customer error', () => {
    mockCustomerService.create.mockReturnValue(
      throwError(() => ({ error: { message: 'Duplicate' } })),
    );

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.setValue({
      documentType: 'CC',
      documentNumber: '999',
      fullName: 'Test',
      email: 'test@test.com',
      createAccount: false,
    });

    component.onSubmit();

    expect(component.messageType()).toBe('error');
  });

  it('should handle create account error after customer success', () => {
    mockAccountService.create.mockReturnValue(
      throwError(() => ({ error: { message: 'Account error' } })),
    );

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.setValue({
      documentType: 'CC',
      documentNumber: '999',
      fullName: 'Test',
      email: 'test@test.com',
      createAccount: true,
    });

    component.onSubmit();

    expect(component.messageType()).toBe('error');
  });

  it('should not submit if form is invalid', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    fixture.componentInstance.onSubmit();

    expect(mockCustomerService.create).not.toHaveBeenCalled();
  });
});
