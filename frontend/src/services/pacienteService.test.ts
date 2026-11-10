import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from './api';
import { pacienteService } from './pacienteService';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('pacienteService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve listar pacientes', async () => {
    const mockPacientes = [{ idPaciente: '1', nomePessoa: 'Lucas Silva', apelidoPaciente: 'Luquinhas' }];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockPacientes });

    const result = await pacienteService.list();

    expect(api.get).toHaveBeenCalledWith('/pacientes');
    expect(result).toEqual(mockPacientes);
  });

  it('deve carregar paciente por ID', async () => {
    const mockPaciente = { idPaciente: '1', nomePessoa: 'Lucas Silva' };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockPaciente });

    const result = await pacienteService.getById('1');

    expect(api.get).toHaveBeenCalledWith('/pacientes/1');
    expect(result).toEqual(mockPaciente);
  });

  it('deve criar paciente', async () => {
    const input = { nomePessoa: 'Lucas Silva', cpf: '12345678901', isResponsavel: false };
    const mockCreated = { idPaciente: '1', ...input };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockCreated });

    const result = await pacienteService.create(input);

    expect(api.post).toHaveBeenCalledWith('/pacientes', input);
    expect(result).toEqual(mockCreated);
  });

  it('deve atualizar paciente', async () => {
    const input = { nomePessoa: 'Lucas Editado', isResponsavel: true };
    const mockUpdated = { idPaciente: '1', ...input };
    vi.mocked(api.put).mockResolvedValueOnce({ data: mockUpdated });

    const result = await pacienteService.update('1', input);

    expect(api.put).toHaveBeenCalledWith('/pacientes/1', input);
    expect(result).toEqual(mockUpdated);
  });

  it('deve deletar paciente', async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: {} });

    await pacienteService.delete('1');

    expect(api.delete).toHaveBeenCalledWith('/pacientes/1');
  });

  it('deve listar planos de saúde tratando erros', async () => {
    const mockPlanos = [{ idPlanoSaude: 'p1', nomePlano: 'Unimed' }];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockPlanos });

    const result = await pacienteService.listPlanos();
    expect(result).toEqual(mockPlanos);

    vi.mocked(api.get).mockRejectedValueOnce(new Error('Network error'));
    const fallback = await pacienteService.listPlanos();
    expect(fallback).toEqual([]);
  });

  it('deve listar clinicas tratando erros', async () => {
    const mockClinicas = [{ idClinica: 'c1', nomeClinica: 'Clínica A' }];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockClinicas });

    const result = await pacienteService.listClinicas();
    expect(result).toEqual(mockClinicas);

    vi.mocked(api.get).mockRejectedValueOnce(new Error('Network error'));
    const fallback = await pacienteService.listClinicas();
    expect(fallback).toEqual([]);
  });

  it('deve listar responsaveis mapeando nomeResponsavel e cpf corretamente', async () => {
    const mockResps = [
      { idResponsavel: 'r1', nomeResponsavel: 'Viviane Silva', cpf: '11122233344' },
    ];
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockResps });

    const result = await pacienteService.listResponsaveis();
    expect(result).toEqual([
      { idResponsavel: 'r1', nome: 'Viviane Silva', cpf: '11122233344' },
    ]);
  });
});
