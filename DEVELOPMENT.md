#  Руководство по разработке

##  Быстрый старт

### 1. Клонирование проекта
```bash
  git clone <url-репозитория>
  cd platform-for-reselling-things
```
# Копируем шаблон настроек
cp .env.example .env.local

# Редактируем под свои нужды (пароли, порты и т.д.)
# Используйте любой текстовый редактор:
# - Windows: notepad .env.local
# - Mac/Linux: nano .env.local или vim .env.local

# Запускаем PostgreSQL и pgAdmin
docker-compose -f docker-compose.dev.yml up -d

# Проверяем что контейнеры запущены
docker-compose -f docker-compose.dev.yml ps

# Останавливаем (когда не нужны)
docker-compose -f docker-compose.dev.yml down

# Останавливаем и удаляем данные (осторожно!)
docker-compose -f docker-compose.dev.yml down -v