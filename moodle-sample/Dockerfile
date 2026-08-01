FROM php:8.3-apache

# Dependências de sistema + extensões PHP exigidas pelo Moodle
RUN apt-get update && apt-get install -y \
        git unzip pkg-config \
        libicu-dev libxml2-dev libzip-dev libpng-dev libonig-dev \
        libcurl4-openssl-dev libjpeg62-turbo-dev libfreetype6-dev zlib1g-dev \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install intl mbstring xml curl zip gd mysqli soap opcache exif \
    && a2enmod rewrite \
    && rm -rf /var/lib/apt/lists/*

# Código-fonte oficial do Moodle (branch estável 4.5)
RUN git clone --branch MOODLE_405_STABLE --depth 1 https://github.com/moodle/moodle.git /var/www/html

# Ajuste de php.ini exigido pelo Moodle (max_input_vars >= 5000)
RUN echo "max_input_vars = 5000" > /usr/local/etc/php/conf.d/moodle.ini

# Pasta de dados do Moodle (fora do webroot, como recomendado)
RUN mkdir -p /var/www/moodledata \
    && chown -R www-data:www-data /var/www/moodledata /var/www/html \
    && chmod -R 755 /var/www/moodledata

EXPOSE 80