import { createContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authService } from '../services/authService';
import type { LoginCredentials, RegisterInput } from '../types/auth';
import type { UserDTO } from '../types/user';

const TOKEN_KEY = 'dailyautism_token';

export interface AuthContextType {
  user: UserDTO | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isResponsavel: boolean;
  isProfissional: boolean;
  isFuncionarioClinica: boolean;
  isAdminClinica: boolean;
  loading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  loginWithGoogle?: (idToken: string) => Promise<void>;
  refreshUser?: () => Promise<void>;
  register: (payload: RegisterInput) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [loading, setLoading] = useState<boolean>(!!localStorage.getItem(TOKEN_KEY));

  const isAuthenticated = !!token && !!user;
  const isAdmin = Boolean(user?.isAdmin || (user as any)?.admin || user?.perfis?.some((p) => p === 'ADMIN' || p === 'ROLE_ADMIN'));
  const isResponsavel = Boolean(user?.isResponsavel || (user as any)?.responsavel || user?.perfis?.some((p) => p === 'RESPONSAVEL' || p === 'ROLE_RESPONSAVEL'));
  const isProfissional = Boolean(user?.isProfissional || (user as any)?.profissional || user?.perfis?.some((p) => p === 'PROFISSIONAL' || p === 'ROLE_PROFISSIONAL'));
  const isAdminClinica = Boolean(user?.isAdminClinica || (user as any)?.adminClinica || user?.perfis?.some((p) => p === 'ADMIN_CLINICA' || p === 'ROLE_ADMIN_CLINICA'));
  const isFuncionarioClinica = Boolean(
    isAdminClinica ||
    user?.isFuncionarioClinica ||
    (user as any)?.funcionarioClinica ||
    user?.perfis?.some((p) =>
      p === 'CLINICA' || p === 'ROLE_CLINICA' ||
      p === 'FUNCIONARIO_CLINICA' || p === 'ROLE_FUNCIONARIO_CLINICA' ||
      p === 'ADMIN_CLINICA' || p === 'ROLE_ADMIN_CLINICA'
    )
  );

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    localStorage.removeItem(TOKEN_KEY);
  }, []);

  const login = useCallback(async (credentials: LoginCredentials) => {
    const response = await authService.login(credentials);
    const newToken = response.token;
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);

    const userProfile = await authService.getMe();
    setUser(userProfile);
  }, []);

  const loginWithGoogle = useCallback(async (idToken: string) => {
    const response = await authService.loginWithGoogle(idToken);
    const newToken = response.token;
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);

    const userProfile = await authService.getMe();
    setUser(userProfile);
  }, []);

  const refreshUser = useCallback(async () => {
    const userProfile = await authService.getMe();
    setUser(userProfile);
  }, []);

  const register = useCallback(async (payload: RegisterInput) => {
    const response = await authService.register(payload);
    const newToken = response.token;
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);

    const userProfile = await authService.getMe();
    setUser(userProfile);
  }, []);

  // On mount, if token exists in localStorage, validate it by fetching user profile
  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (storedToken) {
      setToken(storedToken);
      setLoading(true);
      authService
        .getMe()
        .then((userProfile) => {
          setUser(userProfile);
        })
        .catch(() => {
          logout();
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Listen for unauthorized events from the API interceptor
  useEffect(() => {
    const handleUnauthorized = () => {
      logout();
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [logout]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isAdmin,
        isResponsavel,
        isProfissional,
        isFuncionarioClinica,
        isAdminClinica,
        loading,
        login,
        loginWithGoogle,
        refreshUser,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
