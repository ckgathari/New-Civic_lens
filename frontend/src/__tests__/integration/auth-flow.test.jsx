import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import SignupPage from '../../pages/SignupPage';

vi.mock('../../api/apiClient', () => ({
  authAPI: {
    signup: vi.fn(),
  },
  locationsAPI: {
    getCounties: vi.fn().mockResolvedValue({ data: [] }),
    getConstituencies: vi.fn().mockResolvedValue({ data: [] }),
    getWards: vi.fn().mockResolvedValue({ data: [] }),
  },
}));

const renderSignupPage = () => {
  return render(
    <BrowserRouter>
      <SignupPage />
    </BrowserRouter>
  );
};

describe('SignupPage Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should complete step 1 and advance to step 2 (location selector)', async () => {
    const user = userEvent.setup();
    renderSignupPage();

    // Step 1: fill all required fields
    await user.type(screen.getByPlaceholderText(/first name/i), 'John');
    await user.type(screen.getByPlaceholderText(/last name/i), 'Doe');
    await user.type(screen.getByPlaceholderText(/email/i), 'newuser@example.com');
    await user.type(screen.getByPlaceholderText(/password/i), 'SecurePass123!');

    await user.click(screen.getByRole('button', { name: /next/i }));

    // Step 2 should now appear
    await waitFor(() => {
      expect(screen.getByText(/select your location/i)).toBeInTheDocument();
    });
  });

  it('should show location selector dropdowns in step 2', async () => {
    const user = userEvent.setup();
    renderSignupPage();

    // Complete step 1
    await user.type(screen.getByPlaceholderText(/first name/i), 'Jane');
    await user.type(screen.getByPlaceholderText(/last name/i), 'Smith');
    await user.type(screen.getByPlaceholderText(/email/i), 'jane@example.com');
    await user.type(screen.getByPlaceholderText(/password/i), 'SecurePass123!');
    await user.click(screen.getByRole('button', { name: /next/i }));

    // Step 2: county, constituency, ward selects should be present
    await waitFor(() => {
      expect(screen.getByDisplayValue(/select county/i)).toBeInTheDocument();
      expect(screen.getByDisplayValue(/select constituency/i)).toBeInTheDocument();
    });
  });

  it('should render all required step 1 fields', () => {
    renderSignupPage();

    expect(screen.getByPlaceholderText(/first name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/last name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /next/i })).toBeInTheDocument();
  });
});
