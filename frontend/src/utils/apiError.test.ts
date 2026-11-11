import { describe, it, expect } from 'vitest';
import { extractApiErrorMessage, extractInactiveConflict } from './apiError';

describe('apiError helpers', () => {
  it('extrai conflito de recurso inativo quando status é 409 e errorCode é INACTIVE_RESOURCE_EXISTS', () => {
    const err = {
      response: {
        status: 409,
        data: {
          errorCode: 'INACTIVE_RESOURCE_EXISTS',
          errorMessage: 'Já existe uma clínica desativada com este CNPJ.',
          resourceType: 'CLINICA',
          resourceId: 'cli-123',
          resourceName: 'Clínica Modelo',
        },
      },
    };

    expect(extractInactiveConflict(err)).toEqual({
      resourceType: 'CLINICA',
      resourceId: 'cli-123',
      resourceName: 'Clínica Modelo',
      message: 'Já existe uma clínica desativada com este CNPJ.',
    });
  });

  it('retorna null quando erro não for INACTIVE_RESOURCE_EXISTS', () => {
    expect(extractInactiveConflict({ response: { status: 400, data: { errorMessage: 'Erro qualquer' } } })).toBeNull();
  });

  it('extrai errorMessage do backend antes do fallback', () => {
    const err = { response: { status: 400, data: { errorMessage: 'Mensagem do backend' } } };
    expect(extractApiErrorMessage(err, 'Fallback')).toBe('Mensagem do backend');
  });
});
