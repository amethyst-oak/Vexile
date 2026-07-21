/usr/lib/jvm/java-8-openjdk-amd64/bin/javac -source 1.8 -target 1.8 -encoding UTF-8 -d build/classes VexileLauncher.java

# 2. Создаем манифест
echo "Manifest-Version: 1.0" > build/manifest.txt
echo "Main-Class: VexileLauncher" >> build/manifest.txt

# 3. Упаковываем в jar
jar cfm dist/VexileLauncher.jar build/manifest.txt -C build/classes .
