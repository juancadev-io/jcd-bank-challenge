import { TestBed } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
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

  it('should render no-account row with create button', () => {
    mockAccountService.getAll.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.no-account')?.textContent).toContain('Sin cuenta');
    expect(el.querySelector('.btn-create')).toBeTruthy();
  });

  it('should render empty state when no customers', () => {
    mockCustomerService.getAll.mockReturnValue(of([]));
    mockAccountService.getAll.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('No hay clientes registrados');
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

  it('should handle create account error with message', () => {
    mockAccountService.create.mockReturnValue(
      throwError(() => ({ error: { message: 'Already has account' } })),
    );

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.createAccount(1);

    expect(fixture.componentInstance.message()).toBe('Already has account');
  });

  it('should handle create account error without message', () => {
    mockAccountService.create.mockReturnValue(throwError(() => new Error('network')));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.createAccount(1);

    expect(fixture.componentInstance.message()).toBe('Error al crear cuenta');
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

  it('should render INACTIVE account with Activar button', () => {
    const inactiveAccount = { ...mockAccount, status: 'INACTIVE' };
    mockAccountService.getAll.mockReturnValue(of([inactiveAccount]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.btn-activate')?.textContent).toContain('Activar');
    expect(el.querySelector('.badge.inactive')).toBeTruthy();
  });

  it('should render ACTIVE account with Bloquear button', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.btn-block')?.textContent).toContain('Bloquear');
    expect(el.querySelector('.badge.active')).toBeTruthy();
  });

  it('should handle toggle status error with message', () => {
    mockAccountService.updateStatus.mockReturnValue(
      throwError(() => ({ error: { message: 'Forbidden' } })),
    );

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.toggleStatus(mockAccount);

    expect(fixture.componentInstance.message()).toBe('Forbidden');
  });

  it('should handle toggle status error without message', () => {
    mockAccountService.updateStatus.mockReturnValue(throwError(() => new Error('network')));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.toggleStatus(mockAccount);

    expect(fixture.componentInstance.message()).toBe('Error al actualizar estado');
  });

  it('should display success message in template', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    fixture.componentInstance.createAccount(1);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.alert.success')).toBeTruthy();
  });

  it('should render loading state', () => {
    const pending$ = new Subject<Customer[]>();
    mockCustomerService.getAll.mockReturnValue(pending$);

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Cargando...');
  });

  it('should trigger createAccount via DOM click', () => {
    mockAccountService.getAll.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const createBtn = el.querySelector('.btn-create') as HTMLButtonElement;
    createBtn.click();

    expect(mockAccountService.create).toHaveBeenCalledWith({ customerId: 1 });
  });

  it('should trigger toggleStatus via DOM click on Bloquear', () => {
    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const blockBtn = el.querySelector('.btn-block') as HTMLButtonElement;
    blockBtn.click();

    expect(mockAccountService.updateStatus).toHaveBeenCalledWith(1, 'INACTIVE');
  });

  it('should trigger toggleStatus via DOM click on Activar', () => {
    const inactiveAccount = { ...mockAccount, status: 'INACTIVE' };
    mockAccountService.getAll.mockReturnValue(of([inactiveAccount]));

    const fixture = TestBed.createComponent(AccountsPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const activateBtn = el.querySelector('.btn-activate') as HTMLButtonElement;
    activateBtn.click();

    expect(mockAccountService.updateStatus).toHaveBeenCalledWith(1, 'ACTIVE');
  });
});
