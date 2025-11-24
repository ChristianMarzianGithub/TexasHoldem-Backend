# Build stage
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* tsconfig*.json vite.config.ts index.html ./
COPY src ./src
RUN npm install
RUN npm run build

# Runtime stage
FROM nginx:alpine
ENV PORT=8080
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
CMD ["/bin/sh", "-c", "sed -i 's/listen 80;/listen ${PORT};/' /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
