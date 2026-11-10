export interface ProfissionalResumo {
  idProf: string;
  nomeProfissional: string;
  conselhoProf?: string;
}

export interface Arquivo {
  idArquivo: string;
  nomeArquivo: string;
  descricao?: string;
  dataArquivo: string;
  dataUpload: string;
  extensaoArquivo: string;
  urlArquivo?: string;
  idPaciente: string;
  nomePaciente: string;
  idTipoArquivo: string;
  nomeTipoArquivo: string;
  idUser: string;
  nomeUploadadoPor?: string;
  compartilhadoComResponsavel: boolean;
  compartilhadoComClinica: boolean;
  idClinicaCompartilhada?: string;
  nomeClinicaCompartilhada?: string;
  profissionaisCompartilhados: ProfissionalResumo[];
}

export interface SaveArquivoInput {
  nomeArquivo?: string;
  descricao?: string;
  dataArquivo: string;
  extensaoArquivo: string;
  arquivoBase64: string;
  urlArquivo?: string;
  idPaciente: string;
  idTipoArquivo: string;
  compartilhadoComResponsavel?: boolean;
  compartilhadoComClinica?: boolean;
  idClinicaCompartilhada?: string;
  idsProfissionaisCompartilhados?: string[];
}

export interface CompartilharArquivoInput {
  compartilhadoComResponsavel: boolean;
  compartilhadoComClinica: boolean;
  idClinicaCompartilhada?: string;
  idsProfissionaisCompartilhados: string[];
}

export interface TipoArquivo {
  idTipoArquivo: string;
  nomeTipo: string;
  descricaoTipo?: string;
}

export interface ArquivoDownload {
  idArquivo: string;
  nomeArquivo: string;
  extensaoArquivo: string;
  arquivoBase64: string;
}

export interface OpcoesCompartilhamento {
  clinicas: { idClinica: string; nomeClinica: string }[];
  profissionais: { idProf: string; nomeProfissional: string; conselhoProf?: string }[];
}
