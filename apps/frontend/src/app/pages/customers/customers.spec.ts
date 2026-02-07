import { TestBed } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
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

  it('should render customer table', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('table')).toBeTruthy();
    expect(el.textContent).toContain('Juan Perez');
  });

  it('should render empty state when no customers', () => {
    mockCustomerService.getAll.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('No hay clientes registrados');
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
    expect(component.message()).toBe('Cliente creado exitosamente');
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
    expect(component.message()).toBe('Cliente y cuenta creados exitosamente');
  });

  it('should handle create customer error with message', () => {
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

    expect(component.message()).toBe('Duplicate');
  });

  it('should handle create customer error without message', () => {
    mockCustomerService.create.mockReturnValue(throwError(() => new Error('network')));

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

    expect(component.message()).toBe('Error al crear cliente');
  });

  it('should handle create account error after customer success with message', () => {
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

    expect(component.message()).toContain('Account error');
  });

  it('should handle create account error after customer success without message', () => {
    mockAccountService.create.mockReturnValue(throwError(() => new Error('network')));

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

    expect(component.message()).toContain('Error desconocido');
  });

  it('should not submit if form is invalid', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    fixture.componentInstance.onSubmit();

    expect(mockCustomerService.create).not.toHaveBeenCalled();
  });

  it('should disable submit button when form is invalid', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const button = el.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });

  it('should display success message in template', () => {
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
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.alert.success')).toBeTruthy();
  });

  it('should render loading state in list', () => {
    const pending$ = new Subject<Customer[]>();
    mockCustomerService.getAll.mockReturnValue(pending$);

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Cargando...');
  });

  it('should show Creando... text in button when loading', () => {
    const pending$ = new Subject<Customer[]>();
    mockCustomerService.getAll.mockReturnValue(pending$);

    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const button = el.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(button.textContent).toContain('Creando...');
  });

  it('should show Crear Cliente text in button when not loading', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const button = el.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(button.textContent).toContain('Crear Cliente');
  });

  it('should show validation error for required documentNumber', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.get('documentNumber')?.markAsTouched();
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('El numero de documento es obligatorio');
  });

  it('should show validation error for invalid email format', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.get('email')?.setValue('not-an-email');
    component.form.get('email')?.markAsTouched();
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('El formato del email no es valido');
  });

  it('should show required email error when empty', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.get('email')?.markAsTouched();
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('El email es obligatorio');
  });

  it('should submit form via DOM', () => {
    const fixture = TestBed.createComponent(CustomersPage);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.form.setValue({
      documentType: 'CE',
      documentNumber: '888',
      fullName: 'Ana',
      email: 'ana@test.com',
      createAccount: false,
    });
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const button = el.querySelector('button[type="submit"]') as HTMLButtonElement;
    button.click();

    expect(mockCustomerService.create).toHaveBeenCalled();
  });
});
