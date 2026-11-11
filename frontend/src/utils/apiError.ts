export function extractApiErrorMessage(err: unknown, fallback: string): string {
  if (err && typeof err === 'object' && 'response' in err) {
    const res = (
      err as {
        response?: {
          status?: number;
          data?: {
            errorMessage?: string;
            message?: string;
            detail?: string;
            details?: string[];
          };
        };
      }
    ).response;

    const data = res?.data;
    if (data) {
      if (Array.isArray(data.details) && data.details.length > 0) {
        return data.details.join(', ');
      }
      if (data.errorMessage) return data.errorMessage;
      if (data.message) return data.message;
      if (data.detail) return data.detail;
    }

    if (res?.status === 403) {
      return 'Você não tem permissão para realizar esta ação.';
    }
    if (res?.status === 401) {
      return 'Sua sessão expirou. Faça login novamente.';
    }
  }
  return fallback;
}
