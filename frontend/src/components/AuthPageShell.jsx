import React from 'react';

const AuthPageShell = ({ children, className = '' }) => {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
      <div className={`w-full max-w-sm rounded-2xl bg-white p-8 shadow-md ${className}`.trim()}>
        {children}
      </div>
    </div>
  );
};

export default AuthPageShell;
