import { describe, it, expect, vi, beforeEach } from 'vitest';
import { profissionalService } from './profissionalService';
import api from './api';

vi.mock('./api');

describe('profissionalService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('list requests all professionals from GET /profissionais', async () => {
    const mockList = [
      {
        idProf: 'prof-1',
        idPessoa: 'p-1',
        nomeProfissional: 'Dr. Roberto Alves',
        cpf: '12345678900',
        email: 'roberto@clinica.com',
        telefone: '11999998888',
        conselhoProf: 'CRM/SP 123456',
      },
    ];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockList });

    const result = await profissionalService.list();

    expect(api.get).toHaveBeenCalledWith('/profissionais');
    expect(result).toEqual(mockList);
  });

  it('create sends unified professional data to POST /profissionais', async () => {
    const payload = {
      nomePessoa: 'Dra. Camila Psicologia',
      CPFPessoa: '98765432100',
      datanascPessoa: '1990-05-15',
      emailPessoa: 'camila@clinica.com',
      celularPessoa: '11988887777',
      conselhoProf: 'CRP 06/12345',
    };

    const mockCreated = {
      idProf: 'prof-2',
      idPessoa: 'p-2',
      nomeProfissional: payload.nomePessoa,
      cpf: payload.CPFPessoa,
      email: payload.emailPessoa,
      telefone: payload.celularPessoa,
      conselhoProf: payload.conselhoProf,
    };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockCreated });

    const result = await profissionalService.create(payload);

    expect(api.post).toHaveBeenCalledWith('/profissionais', payload);
    expect(result).toEqual(mockCreated);
  });

  it('delete sends DELETE request to /profissionais/{id}', async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: undefined });

    await profissionalService.delete('prof-1');

    expect(api.delete).toHaveBeenCalledWith('/profissionais/prof-1');
  });
});

