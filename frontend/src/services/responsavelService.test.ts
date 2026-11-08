import { describe, it, expect, vi, beforeEach } from 'vitest';
import { responsavelService } from './responsavelService';
import api from './api';

vi.mock('./api');

describe('responsavelService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('list requests all responsaveis from GET /responsaveis', async () => {
    const mockList = [
      {
        idResponsavel: 'resp-1',
        idPessoa: 'p-1',
        nomeResponsavel: 'Maria da Silva',
        cpf: '12345678900',
        email: 'maria@email.com',
        telefone: '11999998888',
        isPaciente: false,
        pacientes: [{ idPaciente: 'pac-1', nomePaciente: 'Lucas Silva' }],
      },
    ];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockList });

    const result = await responsavelService.list();

    expect(api.get).toHaveBeenCalledWith('/responsaveis');
    expect(result).toEqual(mockList);
  });

  it('create sends unified responsavel data to POST /responsaveis', async () => {
    const payload = {
      nomePessoa: 'João da Silva',
      CPFPessoa: '98765432100',
      datanascPessoa: '1985-04-10',
      emailPessoa: 'joao@email.com',
      celularPessoa: '11988887777',
      isPacienteTambem: true,
      pacienteIds: ['pac-1'],
    };

    const mockCreated = {
      idResponsavel: 'resp-2',
      idPessoa: 'p-2',
      nomeResponsavel: payload.nomePessoa,
      cpf: payload.CPFPessoa,
      email: payload.emailPessoa,
      telefone: payload.celularPessoa,
      isPaciente: true,
      pacientes: [{ idPaciente: 'pac-1', nomePaciente: 'Lucas' }],
    };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockCreated });

    const result = await responsavelService.create(payload);

    expect(api.post).toHaveBeenCalledWith('/responsaveis', payload);
    expect(result).toEqual(mockCreated);
  });

  it('delete sends DELETE /responsaveis/:id', async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: null });

    await responsavelService.delete('resp-123');

    expect(api.delete).toHaveBeenCalledWith('/responsaveis/resp-123');
  });

  it('listPacientes requests available patients from GET /pacientes', async () => {
    const mockPacientes = [
      { idPaciente: 'pac-1', nomePessoa: 'Lucas Silva', apelidoPaciente: 'Luquinhas' },
    ];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockPacientes });

    const result = await responsavelService.listPacientes();

    expect(api.get).toHaveBeenCalledWith('/pacientes');
    expect(result).toEqual(mockPacientes);
  });
});
