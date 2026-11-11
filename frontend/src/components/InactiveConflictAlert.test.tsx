import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import InactiveConflictAlert from './InactiveConflictAlert';

describe('InactiveConflictAlert', () => {
  it('renderiza mensagem de conflito e dispara callback de reativação ao clicar', () => {
    const onReactivate = vi.fn();

    render(
      <InactiveConflictAlert
        conflict={{
          resourceType: 'USUARIO',
          resourceId: 'u-1',
          resourceName: 'Maria',
          message: 'Já existe um usuário desativado com este e-mail (Maria).',
        }}
        onReactivate={onReactivate}
      />
    );

    expect(screen.getByText('Cadastro desativado encontrado')).toBeInTheDocument();
    expect(screen.getByText(/Já existe um usuário desativado/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /reativar cadastro existente/i }));
    expect(onReactivate).toHaveBeenCalledTimes(1);
  });
});
