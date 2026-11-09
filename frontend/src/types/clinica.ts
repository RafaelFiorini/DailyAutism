export interface Clinica {
  idClinica: string;
  nomeClinica: string;
  cnpjClinica: string;
  enderecoClinica?: string;
  telefoneClinica?: string;
  whatsappClinica?: string;
  emailClinica?: string;
}

export interface SaveClinicaInput {
  nomeClinica: string;
  cnpjClinica: string;
  enderecoClinica?: string;
  telefoneClinica?: string;
  whatsappClinica?: string;
  emailClinica?: string;
}
