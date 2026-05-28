# ayrik-olay-simulasyonu-uygulamasi 🚀

## Bu Fork Hakkında

Bu repo, orijinal **ayrik-olay-simulasyonu-uygulamasi** projesinin kişisel bir fork'udur.

[![CI](https://github.com/TalatKarasakal/ayrik-olay-simulasyonu-uygulamasi/actions/workflows/ci.yml/badge.svg?branch=enhanced-personal-fork)](https://github.com/TalatKarasakal/ayrik-olay-simulasyonu-uygulamasi/actions/workflows/ci.yml)
[![Latest Release](https://img.shields.io/github/v/release/TalatKarasakal/ayrik-olay-simulasyonu-uygulamasi?filter=v*-enhanced&label=release&color=blue)](https://github.com/TalatKarasakal/ayrik-olay-simulasyonu-uygulamasi/releases)

> ⚠️ **Personal Enhanced Fork — `enhanced-personal-fork` branch'i**
> Bu fork, orijinal projenin `master` branch'inde dokunmaz. `enhanced-personal-fork` branch'inde modern build sistemi (Maven), test framework (JUnit 5) ve CI pipeline eklenmiştir. Kodun çekirdek mantığı orijinal ekibe aittir.

## Proje Hakkında 📝
**ayrik-olay-simulasyonu-uygulamasi**, karmaşık iş akışlarını ve sistem süreçlerini modellemek için tasarlanmış, Java tabanlı gelişmiş bir **Ayrık Olay Simülasyonu (Discrete Event Simulation)** platformudur. Bu uygulama; iş tiplerini (JobTypes), görevleri (Tasks) ve istasyonları (Stations) dinamik olarak analiz ederek sistem performansını ölçer ve darboğazları tespit eder.

Akademik standartlarda geliştirilen bu proje, endüstriyel mühendislik ve yazılım mimarisi prensiplerini bir araya getirerek gerçek zamanlı sistem davranışlarını simüle eder.

## Öne Çıkan Özellikler ✨
- **Dinamik İş Akışı Yapılandırması**: `Workflow` ve `Job` dosyaları üzerinden esnek sistem tanımlama.
- **Gelişmiş Çizelgeleme Algoritmaları**: İstasyon bazlı **FIFO** (First-In-First-Out) ve **EDD** (Earliest Due Date) önceliklendirme desteği.
- **Çoklu Görev Yönetimi (Multitasking)**: İstasyonlarda aynı anda birden fazla görevin işlenebilmesi için `MultiFlag` desteği.
- **Kapasite ve Hız Modelleme**: İstasyon bazlı hız varyasyonları ve kapasite kısıtlamaları ile gerçekçi modelleme.
- **Detaylı Raporlama**: Simülasyon sonunda ortalama gecikme (Average Tardiness) ve istasyon kullanım oranları (Station Utilization) gibi kritik metriklerin hesaplanması.

## Kullanılan Teknolojiler ve Kütüphaneler 🛠️
- **Dil**: Java 17
- **Build**: Apache Maven 3.x
- **Test**: JUnit 5 (Jupiter)
- **Kapsam**: JaCoCo
- **Mimar**: Nesne Yönelimli Programlama (OOP) ve Olay Tabanlı Mimari
- **I/O**: Java File & Scanner API (Özel dosya ayrıştırma mantığı)
- **Veri Yapıları**: HashMap, ArrayList, Streams API

## Geliştirme Süreci 🤖🤝🧑‍💻
Bu proje, modern yazılım geliştirme metodolojileri çerçevesinde bir **Yapay Zeka (AI) Ajanı** desteğiyle, insan-makine iş birliği (Human-in-the-loop) içerisinde geliştirilmiştir. 
- **Mimari Kararlar**: AI desteği ile optimize edilmiş nesne modelleri tasarlanmış, verimlilik ve genişletilebilirlik ön planda tutulmuştur.
- **Kod Optimizasyonu**: Tip güvenliği, hata yönetimi ve algoritma performansı AI analizi ile iyileştirilmiştir.
- **Dokümantasyon**: Teknik dokümantasyon süreci, profesyonel standartları karşılayacak şekilde AI tarafından yapılandırılmıştır.

## Kurulum ve Çalıştırma Talimatları 🚀
Uygulama, komut satırı üzerinden iki adet girdi dosyası (Workflow ve Job dosyaları) ile çalıştırılır.

### Gereksinimler
- Java Development Kit (JDK) 17 veya üzeri.
- Apache Maven 3.6 veya üzeri.

### Adımlar
1. Projeyi bilgisayarınıza klonlayın veya indirin.
2. Proje kök dizininde Maven ile derleyin ve paket oluşturun:
   ```bash
   mvn clean package
   ```
3. Oluşturulan jar dosyasını workflow ve job dosyalarınızı parametre olarak vererek çalıştırın:
   ```bash
   java -jar target/ayrik-olay-simulasyonu-1.0.0-enhanced.jar <workflow_dosyasi>.txt <job_dosyasi>.txt
   ```
   *(Dosya sırası fark etmeksizin uygulama otomatik olarak dosya tiplerini algılayacaktır.)*

### Testleri Çalıştırma
```bash
mvn test
```

### Kapsam Raporu
```bash
mvn test
# Rapor: target/site/jacoco/index.html
```

---

# ayrik-olay-simulasyonu-uygulamasi 🚀

## About the Project 📝
**ayrik-olay-simulasyonu-uygulamasi** is an advanced Java-based **Discrete Event Simulation (DES)** platform designed to model complex workflows and system processes. This application dynamically analyzes Job Types, Tasks, and Stations to measure system performance and identify bottlenecks.

Developed to academic standards, this project combines industrial engineering principles with software architecture to simulate real-world system behaviors.

## Key Features ✨
- **Dynamic Workflow Configuration**: Flexible system definition via `Workflow` and `Job` input files.
- **Advanced Scheduling Algorithms**: Support for Station-based **FIFO** (First-In-First-Out) and **EDD** (Earliest Due Date) prioritization.
- **Multitasking Management**: `MultiFlag` support allows stations to process multiple tasks simultaneously if capacity permits.
- **Capacity and Speed Modeling**: Realistic modeling using station-based speed variations and capacity constraints.
- **Detailed Reporting**: Calculation of critical metrics such as Average Tardiness and Station Utilization upon simulation completion.

## Technologies and Libraries 🛠️
- **Language**: Java 17
- **Build**: Apache Maven 3.x
- **Testing**: JUnit 5 (Jupiter)
- **Coverage**: JaCoCo
- **Architecture**: Object-Oriented Programming (OOP) & Event-Driven Architecture
- **I/O**: Java File & Scanner API (Custom parsing logic)
- **Data Structures**: HashMap, ArrayList, Streams API

## Development Process 🤖🤝🧑‍💻
This project was developed within the framework of modern software development methodologies, utilizing a **Generative AI Agent** in a human-machine collaboration (Human-in-the-loop) model.
- **Architectural Decisions**: Optimized object models were designed with AI assistance, prioritizing efficiency and extensibility.
- **Code Optimization**: Type safety, error handling, and algorithmic performance were enhanced through AI analysis.
- **Documentation**: The technical documentation process was structured by AI to meet professional industry standards.

## Installation and Execution Instructions 🚀
The application is executed via the command line, requiring two input files (Workflow and Job files).

### Prerequisites
- Java Development Kit (JDK) 17 or later.
- Apache Maven 3.6 or later.

### Steps
1. Clone or download the project to your local machine.
2. Build and package using Maven from the project root:
   ```bash
   mvn clean package
   ```
3. Run the application by passing your workflow and job files as parameters:
   ```bash
   java -jar target/ayrik-olay-simulasyonu-1.0.0-enhanced.jar <workflow_file>.txt <job_file>.txt
   ```
   *(The application automatically detects the file types regardless of the order provided.)*

### Running Tests
```bash
mvn test
```

### Coverage Report
```bash
mvn test
# Report: target/site/jacoco/index.html
```
