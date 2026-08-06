FROM node:20-alpine
WORKDIR /app

# Cache dependencies in their own layer so source-only changes don't reinstall packages.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host"]
