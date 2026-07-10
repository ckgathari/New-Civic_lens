import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../../pages/LoginPage';

// Mock the API
vi.mock('../../api/apiClient', () => ({
  authAPI: {
    login: vi.fn(),
    checkToken: vi.fn(),
  },
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

const renderLoginPage = () => {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  );
};

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render login form with email and password fields', () => {
    renderLoginPage();

    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('should not call login API when email is empty', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();
    renderLoginPage();

    const loginButton = screen.getByRole('button', { name: /login/i });
    await user.click(loginButton);

    expect(authAPI.login).not.toHaveBeenCalled();
  });

  it('should not call login API when email format is invalid', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();
    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    await user.type(emailInput, 'invalid-email');

    const loginButton = screen.getByRole('button', { name: /login/i });
    await user.click(loginButton);

    expect(authAPI.login).not.toHaveBeenCalled();
  });

  it('should not call login API when password is empty', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();
    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    await user.type(emailInput, 'test@example.com');

    const loginButton = screen.getByRole('button', { name: /login/i });
    await user.click(loginButton);

    expect(authAPI.login).not.toHaveBeenCalled();
  });

  it('should submit form with valid credentials', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();

    authAPI.login.mockResolvedValue({
      data: {
        token: 'test-token',
        user: { id: 'user1', email: 'test@example.com' },
      },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    const loginButton = screen.getByRole('button', { name: /login/i });
    await user.click(loginButton);

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalledWith('test@example.com', 'password123');
    });
  });

  it('should display error message on failed login', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();

    authAPI.login.mockRejectedValue(new Error('Invalid credentials'));

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');

    const loginButton = screen.getByRole('button', { name: /login/i });
    await user.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it('should disable submit button while loading', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();

    authAPI.login.mockImplementation(() => new Promise(() => {})); // Never resolves

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const loginButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(loginButton);

    await waitFor(() => {
      expect(loginButton).toBeDisabled();
    });
  });

  it('should have link to forgot password page', () => {
    renderLoginPage();

    const forgotLink = screen.getByRole('link', { name: /forgot password/i });
    expect(forgotLink).toHaveAttribute('href', '/forgot-password');
  });

  it('should have link to signup page', () => {
    renderLoginPage();

    const signupLink = screen.getByRole('link', { name: /sign up/i });
    expect(signupLink).toHaveAttribute('href', '/signup');
  });

  it('should clear error message on new login attempt', async () => {
    const { authAPI } = await import('../../api/apiClient');
    const user = userEvent.setup();

    // First attempt fails
    authAPI.login.mockRejectedValueOnce(new Error('Invalid credentials'));
    // Second attempt succeeds
    authAPI.login.mockResolvedValueOnce({
      data: { token: 'test-token', user: { id: 'user1' } },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const loginButton = screen.getByRole('button', { name: /login/i });

    // Trigger a failed login
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });

    // New login attempt clears the previous error
    await user.click(loginButton);

    await waitFor(() => {
      expect(screen.queryByText(/invalid credentials/i)).not.toBeInTheDocument();
    });
  });
});
