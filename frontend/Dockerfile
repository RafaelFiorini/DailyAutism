# Estágio 1: Build da aplicação React com Node.js e Vite
FROM node:22-alpine AS build
WORKDIR /app

# Ativa o pnpm via corepack (versão compatível com o lockfile)
RUN corepack enable && corepack prepare pnpm@11.17.0 --activate

# Copia os manifestos de dependência e configurações do pnpm
COPY package.json pnpm-lock.yaml* pnpm-workspace.yaml* .npmrc* ./

# Instala as dependências do projeto
RUN pnpm install --frozen-lockfile

# Copia todo o código-fonte do frontend
COPY . .

# Argumento opcional para injetar a URL da API no build se necessário
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

# Executa o build de produção (gera a pasta dist/)
RUN pnpm build

# Estágio 2: Servidor Web Nginx ultra leve para servir os arquivos estáticos
FROM nginx:alpine

# Remove a configuração padrão do Nginx e copia a nossa personalizada para SPA
RUN rm -rf /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os artefatos compilados da aplicação React
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
