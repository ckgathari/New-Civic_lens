import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../api/apiClient';

const Navbar = () => {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await authAPI.logout();
    localStorage.removeItem('authToken');
    navigate('/login');
  };

  return (
    <nav className="sticky top-0 z-50 bg-slate-900 text-white shadow-sm">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
        {/* Brand */}
        <Link to="/dashboard" className="text-lg font-bold tracking-tight text-white">
          CivicLens
        </Link>

        {/* Desktop nav */}
        <div className="hidden items-center gap-4 sm:flex">
          <Link to="/dashboard" className="text-sm font-medium text-slate-300 hover:text-white transition">
            Dashboard
          </Link>
          <button
            onClick={handleLogout}
            className="rounded-full bg-red-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-700"
          >
            Logout
          </button>
        </div>

        {/* Mobile hamburger */}
        <button
          className="flex items-center justify-center rounded-md p-2 text-slate-300 hover:bg-slate-800 hover:text-white transition sm:hidden"
          onClick={() => setMenuOpen((o) => !o)}
          aria-label="Toggle menu"
        >
          {menuOpen ? (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          )}
        </button>
      </div>

      {/* Mobile dropdown */}
      {menuOpen && (
        <div className="border-t border-slate-700 bg-slate-900 px-4 pb-4 pt-2 sm:hidden">
          <Link
            to="/dashboard"
            className="block rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white transition"
            onClick={() => setMenuOpen(false)}
          >
            Dashboard
          </Link>
          <button
            onClick={() => { setMenuOpen(false); handleLogout(); }}
            className="mt-1 w-full rounded-lg bg-red-600 px-3 py-2 text-left text-sm font-semibold text-white hover:bg-red-700 transition"
          >
            Logout
          </button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
