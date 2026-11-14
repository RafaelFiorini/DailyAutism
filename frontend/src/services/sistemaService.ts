import api from './api';

export interface TravaBaixaFuturaResponse {
  ativa: boolean;
  mensagem: string;
}

export const sistemaService = {
  async getTravaBaixaFutura(): Promise<TravaBaixaFuturaResponse> {
    const { data } = await api.get<TravaBaixaFuturaResponse>('/sistema/configuracoes/trava-baixa-futura');
    return data;
  },

  async setTravaBaixaFutura(ativa: boolean): Promise<TravaBaixaFuturaResponse> {
    const { data } = await api.patch<TravaBaixaFuturaResponse>(
      '/sistema/configuracoes/trava-baixa-futura',
      { ativa }
    );
    return data;
  },
};
