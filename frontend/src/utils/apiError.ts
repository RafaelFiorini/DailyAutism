export interface InactiveConflictInfo {
  resourceType?: string;
  resourceId: string;
  resourceName?: string;
  message: string;
}

export function extractInactiveConflict(err: unknown): InactiveConflictInfo | null {
  if (!err || typeof err !== 'object' || !('response' in err)) {
    return null;
  }
  const res = (
    err as {
      response?: {
        status?: number;
        data?: {
          errorCode?: string;
          errorMessage?: string;
          message?: string;
          resourceType?: string;
          resourceId?: string;
          resourceName?: string;
        };
      };
    }
  ).response;

  const data = res?.data;
  if (
    res?.status === 409 &&
    data?.errorCode === 'INACTIVE_RESOURCE_EXISTS' &&
    data.resourceId
  ) {
    return {
      resourceType: data.resourceType,
      resourceId: data.resourceId,
      resourceName: data.resourceName,
      message: data.errorMessage || data.message || 'Já existe um cadastro desativado com este identificador.',
    };
  }
  return null;
}

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
