import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import FinanceiroProfissionalPage from './FinanceiroProfissionalPage';

vi.mock('../components/FinanceiroProfissionalTab', () => ({
  default: () => <div data-testid="financeiro-profissional-tab">Conteúdo Financeiro Profissional Tab</div>,
}));

describe('FinanceiroProfissionalPage', () => {
  it('renderiza o cabeçalho dedicado e o componente de extrato do profissional', () => {
    render(<FinanceiroProfissionalPage />);

    expect(screen.getByRole('heading', { name: /meu financeiro & repasses/i })).toBeInTheDocument();
    expect(
      screen.getByText(/acompanhe repasses de múltiplas clínicas, sessões particulares e quitações/i)
    ).toBeInTheDocument();
    expect(screen.getByTestId('financeiro-profissional-tab')).toBeInTheDocument();
  });
});
