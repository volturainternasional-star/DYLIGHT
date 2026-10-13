package com.example.data

object GameSecRepository {

  val introText = """
    GameSec Lab adalah platform belajar dan laboratorium interaktif yang menggabungkan keamanan game, cybersecurity dasar, coding untuk pemula, analisis data pasar crypto dan saham, investigasi digital, serta pelacakan progres belajar.

    Prinsip Utama:
    • Utamakan simulasi yang bisa diuji, bukan artikel panjang saja.
    • Bedakan fakta, asumsi, hipotesis, dan kesimpulan.
    • Tampilkan sumber data dan waktu pembaruan untuk data pasar.
    • Jangan menganggap satu anomali sebagai bukti pasti kecurangan.
    • Semua fitur benar-benar berfungsi, tanpa tombol dekoratif.
    • Transparan: tidak berpura-pura mengakses proses game pihak ketiga jika hanya simulasi.
    • Utamakan privasi: data akun dan portofolio tersimpan lokal dan aman.
  """.trimIndent()

  // 10 Modul Inti Keamanan Game (Spesifikasi Halaman 4)
  val modules: List<LearningModule> = listOf(
    LearningModule(
      id = 1,
      numberLabel = "Modul 1",
      title = "Client dan Server",
      subtitle = "Arsitektur Client-Server",
      content = "Client adalah perangkat yang dipegang pemain (HP atau PC). Server adalah mesin yang dikontrol developer game.\n\nClient menangani rendering, prediksi gerakan, input pemain, dan data sementara. Server menangani status resmi karakter, validasi aksi, ekonomi, dan aturan final permainan.\n\nPrinsip dasar keamanan: server adalah sumber kebenaran (authoritative server). Client hanya mengirim permintaan. Semakin banyak logika penting dipindah ke server, semakin kecil ruang untuk manipulasi di client, namun menambah latency dan beban server.",
      steps = listOf(
        "Client menangani rendering, animasi, prediksi responsif, dan input perangkat.",
        "Server memegang status resmi (posisi resmi, HP, inventory, dan aturan final).",
        "Authoritative Server: Server adalah sumber kebenaran; client hanya pengirim permintaan intent.",
        "Trade-off: Pemindahan logika ke server mengurangi manipulasi, namun menambah latency dan beban komputasi."
      ),
      clientResponsibilities = listOf(
        "Rendering grafik dan pemutaran animasi",
        "Prediksi gerakan lokal agar terasa responsif",
        "Pencatatan input hardware pemain",
        "Penyimpanan data sementara sebelum konfirmasi server"
      ),
      serverResponsibilities = listOf(
        "Status resmi karakter (posisi resmi, HP, inventory)",
        "Validasi keabsahan seluruh aksi permainan",
        "Ekonomi permainan, transaksi, dan perolehan item",
        "Penerapan aturan permainan yang bersifat final"
      ),
      sections = listOf(
        ModuleSection(
          heading = "Pembagian Tanggung Jawab",
          body = "Client adalah perangkat yang dipegang pemain (HP atau PC). Server adalah mesin terpusat yang dikontrol oleh developer game. Keduanya berkomunikasi melalui jaringan data secara asinkron."
        ),
        ModuleSection(
          heading = "Prinsip Sumber Kebenaran (Authoritative Server)",
          body = "Prinsip dasar keamanan arsitektur: server adalah sumber kebenaran (authoritative source). Client hanya mengirim permintaan atau niat aksi (input intent). Server yang menghitung simulasi fisik dan memutuskan apakah aksi tersebut sah."
        ),
        ModuleSection(
          heading = "Dilema Latency vs Keamanan",
          body = "Semakin banyak logika penting dipindahkan ke server, semakin kecil ruang untuk manipulasi di client. Namun pemindahan ini berkonsekuensi pada penambahan latency yang dirasakan pemain serta peningkatan beban komputasi server secara eksponensial."
        )
      ),
      keyTakeaway = "Server memegang status resmi; client hanya perangkat presentasi dan pengirim permintaan input."
    ),

    LearningModule(
      id = 2,
      numberLabel = "Modul 2",
      title = "Memory & Runtime",
      subtitle = "RAM dan Struktur Memori",
      content = "RAM menampung variabel saat game berjalan (runtime). Pemahaman stack dan heap secara konseptual sangat penting untuk memahami mengapa proses yang berjalan di perangkat pemain dapat dibaca atau dimodifikasi oleh proses lain jika tidak memiliki proteksi isolasi OS.",
      steps = listOf(
        "RAM menyimpan variabel dinamis: koordinat karakter, sisa amunisi, status HP lokal.",
        "Stack digunakan untuk alokasi fungsi lokal yang cepat, sedangkan Heap menampung objek dinamis.",
        "Akses debugger / root memungkinkan proses asing memindai pola nilai memori di RAM.",
        "Proteksi memori lokal: obfuscation, pointer encryption, dan deteksi debugger."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Alokasi Memori Game",
          body = "Setiap variabel dalam game disimpan di alamat memori (pointer) tertentu di RAM. Saat nilai HP berkurang, sistem menulis nilai baru pada blok memori tersebut."
        ),
        ModuleSection(
          heading = "Bagaimana Memory Editing Bekerja",
          body = "Aplikasi analisis memori mencari alamat dengan menyaring nilai yang berubah (misal: mencari nilai 100, lalu setelah terkena damage mencari nilai 80). Setelah alamat ditemukan, nilai dapat dikunci (freeze) atau diubah."
        ),
        ModuleSection(
          heading = "Mitigasi Runtime",
          body = "Enkripsi variabel di RAM, pengacakan struktur data, pengecekan integritas pointer, dan validasi ulang status akhir di server."
        )
      ),
      keyTakeaway = "Semua data yang berada di RAM perangkat lokal memiliki potensi dibaca pengguna; nilai kritis wajib divalidasi server."
    ),

    LearningModule(
      id = 3,
      numberLabel = "Modul 3",
      title = "Komunikasi Jaringan",
      subtitle = "Network & Packet Serialization",
      content = "Komunikasi game multiplayer menggunakan protokol TCP dan UDP. Pemahaman port, request/response, serialisasi biner, serta autentikasi dan enkripsi mutlak dibutuhkan untuk melindungi transmisi data dari manipulasi Man-in-the-Middle.",
      steps = listOf(
        "UDP digunakan untuk paket gerak frekuensi tinggi karena latency rendah.",
        "TCP digunakan untuk transaksi ekonomi, inventory, dan otentikasi akun.",
        "Serialisasi mengubah objek game menjadi urutan byte payload jaringan.",
        "Paket harus menyertakan sequence number dan checksum kriptografis."
      ),
      sections = listOf(
        ModuleSection(
          heading = "TCP vs UDP pada Game",
          body = "Game real-time mengandalkan UDP untuk meminimalkan jeda (karena tidak menunggu retransmisi paket yang hilang), sedangkan aksi kritis mengandalkan TCP atau lapisan reliability kustom di atas UDP."
        ),
        ModuleSection(
          heading = "Risiko Packet Tampering & Replay",
          body = "Jika paket tidak memiliki nomor urut berurutan atau token unik sesi, penyerang dapat menangkap paket sukses (misal: paket klaim reward) lalu mengirimkannya kembali secara berulang (replay attack)."
        )
      ),
      keyTakeaway = "Enkripsi melindungi paket di perjalanan, tetapi tidak menjamin keaslian data jika data di-tamper sebelum enkripsi."
    ),

    LearningModule(
      id = 4,
      numberLabel = "Modul 4",
      title = "File Integrity",
      subtitle = "Integritas Berkas dan Verifikasi Hash",
      content = "Memeriksa keaslian berkas instalasi, manifest aset, tanda tangan digital, serta memahami batasan pemeriksaan integritas.",
      steps = listOf(
        "Kalkulasi hash kriptografis (SHA-256) pada berkas biner dan aset 3D.",
        "Manifest file mencatat hash referensi yang ditandatangani oleh developer.",
        "Batasan integritas: Jika pemeriksa integritas itu sendiri berjalan di client, fungsi pemeriksa dapat dibajak."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Fungsi Hash Satu Arah",
          body = "Perubahan 1 byte saja pada berkas model atau script akan menghasilkan output hash yang berbeda total (avalanche effect)."
        ),
        ModuleSection(
          heading = "Batasan Proteksi Client-Side Hash",
          body = "Pemeriksaan integritas berkas adalah pertahanan lini pertama, namun bukan solusi ajaib jika attacker mem-bypass fungsi pengecekan itu sendiri."
        )
      ),
      keyTakeaway = "Integritas berkas membuktikan berkas belum dimodifikasi, namun verifikasi harus didukung oleh validasi perilaku server."
    ),

    LearningModule(
      id = 5,
      numberLabel = "Modul 5",
      title = "Programming Logic & Validation",
      subtitle = "Logika Pemrograman dan State Machine",
      content = "Membangun logika game yang tangguh dengan state machine, validasi input, boundary checking, dan penanganan kesalahan (error handling) yang ketat.",
      steps = listOf(
        "State machine memastikan transisi status karakter sah (contoh: tidak bisa menembak saat status sedang reload).",
        "Validasi tipe data dan batas nilai (boundary conditions) mencegah overflow.",
        "Error handling aman: jangan sampai kegagalan memicu kondisi 'fail-open'."
      ),
      sections = listOf(
        ModuleSection(
          heading = "State Machine Authoritative",
          body = "Karakter tidak boleh dapat melompat jika statusnya sedang 'jatuh bebas' atau 'stunned'. Validasi kondisi transisi status harus diuji di sisi server."
        )
      ),
      keyTakeaway = "Logika pemrograman yang disiplin mencegah eksploitasi logika game (logic bugs)."
    ),

    LearningModule(
      id = 6,
      numberLabel = "Modul 6",
      title = "Anti-Cheat Systems",
      subtitle = "Prinsip dan Analisis Perilaku Anti-Cheat",
      content = "Bagaimana sistem anti-cheat beroperasi: server authority, validasi gerakan, cooldown, inventory, analisis perilaku (heuristics), dan pengelolaan false positives.",
      steps = listOf(
        "Server-side validation: Menilai apakah aksi masuk akal secara kinematika.",
        "Behavioral heuristics: Menganalisis kurva bidikan dan variansi klik manusia.",
        "Manajemen false positive: Mengakomodasi lag spike jaringan sebelum menjatuhkan sanksi."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Tantangan False Positive",
          body = "Pemain dengan koneksi buruk (packet burst) dapat terlihat seolah berpindah tempat seketika. Sistem deteksi yang matang memberi bobot risiko secara akumulatif sebelum menjatuhkan hukuman."
        )
      ),
      keyTakeaway = "Anti-cheat modern bukan sekadar program pemblokir, melainkan sistem statistik penilaian risiko."
    ),

    LearningModule(
      id = 7,
      numberLabel = "Modul 7",
      title = "Backend & Database",
      subtitle = "Transaksi Data dan Audit Logging",
      content = "Arsitektur backend game: HTTP REST/WebSocket, transaksi SQL dengan integritas ACID, session management, dan pencatatan audit log yang tidak dapat diubah.",
      steps = listOf(
        "Transaksi database mencegah duplikasi item (duplication glitch).",
        "Session token bertanda tangan kriptografis menjaga otentikasi pemain.",
        "Audit log mencatat riwayat pertukaran item dan mata uang virtual."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Race Conditions dan Transaksi ACID",
          body = "Dua permintaan perdagangan item yang dikirim bersamaan dalam fraksi milidetik dapat memicu duplikasi jika database tidak menerapkan kunci baris (row locking) yang tepat."
        )
      ),
      keyTakeaway = "Integritas ekonomi game bertumpu pada desain transaksi database yang terisolasi."
    ),

    LearningModule(
      id = 8,
      numberLabel = "Modul 8",
      title = "Web & API Security",
      subtitle = "Keamanan API dan Pengelolaan Secret",
      content = "Melindungi antarmuka web dan API layanan game dari SQL Injection, Cross-Site Scripting (XSS), CSRF, dan kebocoran API secret.",
      steps = listOf(
        "Parameterized queries untuk mencegah SQL Injection.",
        "Pemisahan secret API di lingkungan backend, jangan pernah diletakkan di bundle frontend.",
        "Sandbox lingkungan eksekusi untuk mencegah code execution liar."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Pengelolaan Secret yang Benar",
          body = "Kredensial API eksternal dan token server hanya boleh tersimpan di backend melalui environment variable tervalidasi."
        )
      ),
      keyTakeaway = "API backend game harus memperlakukan semua input luar sebagai data yang tidak terpercaya."
    ),

    LearningModule(
      id = 9,
      numberLabel = "Modul 9",
      title = "Forensik Digital",
      subtitle = "Analisis Log dan Chain of Custody",
      content = "Prinsip investigasi insiden digital: metadata, timestamp, korelasi waktu, rekonstruksi kronologi kejadian, dan penjagaan integritas bukti.",
      steps = listOf(
        "Sinkronisasi waktu: Memperhatikan zona waktu (UTC) dan koreksi offset jam perangkat.",
        "Korelasi multi-sumber: Membandingkan log server dengan laporan telemetri client.",
        "Menghindari bias: Mengidentifikasi data yang hilang sebelum menarik kesimpulan."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Rekonstruksi Kejadian",
          body = "Setiap entri log harus mencatat ID kejadian, pengidentifikasi pengguna, koordinat, dan cap waktu berpresisi tinggi."
        )
      ),
      keyTakeaway = "Bukti forensik yang kuat memerlukan urutan waktu yang konsisten dan rantai verifikasi data yang utuh."
    ),

    LearningModule(
      id = 10,
      numberLabel = "Modul 10",
      title = "Berpikir Kritis & Verifikasi",
      subtitle = "Membedakan Fakta, Opini, dan Bias",
      content = "Menganalisis klaim keamanan secara objektif: membedakan fakta vs opini, korelasi vs sebab-akibat, bias konfirmasi, dan menangani klaim dengan bukti tidak lengkap.",
      steps = listOf(
        "Fakta: Data log yang terverifikasi dan dapat direproduksi.",
        "Korelasi vs Kausalitas: Dua kejadian bersamaan belum tentu saling menyebabkan.",
        "Klaim tanpa bukti independen tidak boleh langsung dianggap valid atau sebaliknya."
      ),
      sections = listOf(
        ModuleSection(
          heading = "Metodologi Analisis Klaim",
          body = "Sebelum menyatakan sesuatu sebagai anomali berbahaya, telusuri penjelasan alternatif yang lebih sederhana seperti gangguan latensi jaringan atau bug fisika lokal."
        )
      ),
      keyTakeaway = "Keamanan yang baik dibangun dari skeptisisme metodologis dan pengujian empiris, bukan asumsi sepihak."
    )
  )

  val staticModuleList: List<Module> = modules.map {
    Module(title = it.title, content = it.content, steps = it.steps)
  }

  // === 4. Analisis Log Latihan (Dataset Forensik) ===
  val sampleForensicLogs: List<ForensicLogItem> = listOf(
    ForensicLogItem(
      id = "LOG-01",
      timestamp = "14:02:01.120 UTC",
      userId = "USR-4019",
      action = "MOVE_STEP",
      coordinates = "x:10.2, y:20.4, z:0.0",
      result = "ACCEPTED",
      source = "Client Input",
      isSuspicious = false
    ),
    ForensicLogItem(
      id = "LOG-02",
      timestamp = "14:02:01.170 UTC",
      userId = "USR-4019",
      action = "MOVE_STEP",
      coordinates = "x:10.5, y:20.7, z:0.0",
      result = "ACCEPTED",
      source = "Client Input",
      isSuspicious = false
    ),
    ForensicLogItem(
      id = "LOG-03",
      timestamp = "14:02:01.220 UTC",
      userId = "USR-4019",
      action = "MOVE_STEP",
      coordinates = "x:88.4, y:92.1, z:0.0",
      result = "REJECTED (Distance Delta 102m in 50ms)",
      source = "Server Validation Engine",
      isSuspicious = true,
      anomalyReason = "Delta jarak 102m dalam 50ms melampaui batas fisik karakter (v_max = 8.0 m/s)."
    ),
    ForensicLogItem(
      id = "LOG-04",
      timestamp = "14:02:01.222 UTC",
      userId = "USR-4019",
      action = "SERVER_ROLLBACK",
      coordinates = "x:10.5, y:20.7, z:0.0",
      result = "EXECUTED",
      source = "Server Authority",
      isSuspicious = false,
      anomalyReason = "Koreksi posisi paksa kembali ke titik resmi terakhir."
    ),
    ForensicLogItem(
      id = "LOG-05",
      timestamp = "14:02:01.550 UTC",
      userId = "USR-4019",
      action = "FIRE_WEAPON",
      coordinates = "x:10.5, y:20.7, z:0.0",
      result = "FLAGGED (Cooldown Remaining 1.2s)",
      source = "Combat Subsystem",
      isSuspicious = true,
      anomalyReason = "Tembakan dilaporkan sebelum masa refire rate 2.0s selesai."
    ),
    ForensicLogItem(
      id = "LOG-06",
      timestamp = "14:02:01.800 UTC",
      userId = "USR-8821",
      action = "TRANSACTION_TRANSFER",
      coordinates = "Lobby_Trade_Zone",
      result = "ACCEPTED (Balance Verified)",
      source = "Economy Ledger",
      isSuspicious = false
    )
  )

  // === 5. Kasus Investigasi Digital (Halaman 5-6) ===
  val sampleInvestigationCases: List<InvestigationCase> = listOf(
    InvestigationCase(
      id = "CASE-01",
      title = "Kasus 1: Gerakan Melampaui Batas vs Latensi Tinggi",
      problemDescription = "Server mendeteksi lompatan posisi karakter sejauh 40 meter dalam satu tick 50ms pada pemain peringkat tinggi, memicu alarm deteksi kecepatan.",
      investigationQuestion = "Apakah kejadian ini merupakan tindakan manipulasi koordinat lokal, ataukah anomali akibat lonjakan paket (packet loss burst)?",
      evidenceList = listOf(
        EvidenceItem("EV-1A", "Server Kinematic Alert", "Anti-cheat Engine", "14:20:00.050", "Delta jarak = 40m, delta_t = 0.05s", true),
        EvidenceItem("EV-1B", "Ping & Jitter Log", "Network Telemetry", "14:19:59.000", "Latency melonjak dari 32ms ke 840ms selama 1.2 detik sebelum kejadian", true),
        EvidenceItem("EV-1C", "Laporan Rekan Sesi", "Peer Client Log", "14:20:00.100", "Karakter terlihat berjalan di tempat selama 1 detik lalu muncul seketika", true)
      ),
      alternativeExplanations = listOf(
        "Pemain menggunakan speed hack di memori lokal.",
        "Koneksi internet pemain mengalami buffer delay; paket input menumpuk di socket dan terkirim sekaligus saat koneksi pulih (packet burst)."
      ),
      actualFactSummary = "Data latensi membuktikan adanya packet loss burst 840ms. Gerakan tersebut merupakan kompensasi desinkronisasi jaringan, bukan kesengajaan manipulasi."
    ),
    InvestigationCase(
      id = "CASE-02",
      title = "Kasus 2: Klaim Item Gratis dengan Permintaan Kata Sandi",
      problemDescription = "Beredar situs web yang mengklaim dapat menginjeksikan 5000 mata uang game langka ke akun pengguna dengan syarat memasukkan username dan password akun game.",
      investigationQuestion = "Apakah situs tersebut memiliki kemampuan teknis menginjeksi aset, ataukah merupakan teknik pengelabuan kredensial (phishing)?",
      evidenceList = listOf(
        EvidenceItem("EV-2A", "Source Code Halaman Web", "Web Inspector", "09:12:00", "Form HTML mengirimkan nilai input 'password' ke script Discord Webhook eksternal", true),
        EvidenceItem("EV-2B", "Arsitektur Database Game", "Developer Whitepaper", "Official", "Seluruh saldo mata uang disimpan di server ledger terisolasi, tidak ada API eksternal yang menerima password mentah", true)
      ),
      alternativeExplanations = listOf(
        "Situs memiliki celah private exploit ke database resmi.",
        "Situs hanya mengumpulkan kredensial pengguna tanpa pernah memberikan item apa pun (phishing)."
      ),
      actualFactSummary = "Formulir secara terbuka mengirimkan password ke webhook Discord luar. Tidak ada koneksi apa pun ke database game."
    ),
    InvestigationCase(
      id = "CASE-03",
      title = "Kasus 3: Tangkapan Layar Transaksi vs Database Resmi",
      problemDescription = "Seorang pengguna mengunggah tangkapan layar bukti transfer koin game senilai 10.000 koin dan menuntut barang diberikan.",
      investigationQuestion = "Apakah transaksi tersebut sah dan benar-benar tercatat di buku besar database resmi?",
      evidenceList = listOf(
        EvidenceItem("EV-3A", "Gambar Screenshot Pengguna", "Chat Support", "16:45:10", "Tangkapan layar menunjukkan saldo koin bertambah 10.000", false),
        EvidenceItem("EV-3B", "Database Audit Log", "PostgreSQL Read Replica", "16:45:10", "Tidak ditemukan riwayat INSERT pada tabel 'coin_transactions' untuk User ID terkait", true)
      ),
      alternativeExplanations = listOf(
        "Database mengalami kegagalan tulis sementara.",
        "Pengguna memodifikasi tampilan teks saldo melalui DevTools / memory editor lokal perangkat."
      ),
      actualFactSummary = "Database transaksi resmi kosong. Tampilan saldo hanya diubah di lapisan presentasi lokal perangkat pengguna."
    ),
    InvestigationCase(
      id = "CASE-04",
      title = "Kasus 4: Klaim 'AI Anti-Cheat 100% Akurat Tanpa False Positive'",
      problemDescription = "Sebuah vendor perangkat lunak memasarkan solusi anti-cheat yang diklaim mampu mendeteksi 100% kecurangan tanpa kemungkinan false positive.",
      investigationQuestion = "Apakah klaim akurasi 100% absolut tanpa kesalahan dapat diverifikasi secara ilmiah?",
      evidenceList = listOf(
        EvidenceItem("EV-4A", "Brosur Pemasaran", "Marketing Deck", "Terbaru", "Menjanjikan zero false-positive rate pada segala kondisi game", false),
        EvidenceItem("EV-4B", "Statistik Teori Deteksi", "Academic Research", "Peer-reviewed", "Deteksi probabilistik berbasis machine learning selalu memiliki trade-off antara presisi dan recall", true)
      ),
      alternativeExplanations = listOf(
        "Vendor berhasil memecahkan batasan matematika statistik.",
        "Klaim pemasaran hiperbolik yang mengabaikan kenyataan noise data jaringan dan variansi manusia."
      ),
      actualFactSummary = "Klaim 100% akurasi absolut bertentangan dengan prinsip dasar komputasi dan tidak didukung bukti empiris independen."
    ),
    InvestigationCase(
      id = "CASE-05",
      title = "Kasus 5: Ketidakcocokan Waktu Dua Log Berbeda",
      problemDescription = "Log client mencatat aksi pada pukul 12:00:00 sedangkan log server mencatat aksi yang sama pada 05:00:00, menimbulkan dugaan manipulasi waktu.",
      investigationQuestion = "Apakah selisih waktu 7 jam merupakan indikasi manipulasi jam, ataukah perbedaan representasi zona waktu?",
      evidenceList = listOf(
        EvidenceItem("EV-5A", "Client Timestamp", "Device Log", "12:00:00 (WIB / UTC+7)", "Jam internal smartphone pemain di Jakarta", true),
        EvidenceItem("EV-5B", "Server Timestamp", "GCP Server Log", "05:00:00 (UTC+0)", "Catatan waktu standar server internasional", true)
      ),
      alternativeExplanations = listOf(
        "Pemain memundurkan jam HP untuk mempercepat cooldown.",
        "Log client mencatat waktu lokal WIB (UTC+7), sedangkan server mencatat waktu standar UTC+0. Keduanya identik jika dikonversi."
      ),
      actualFactSummary = "Perbedaan 7 jam tepat sesuai dengan offset UTC+7 (WIB) terhadap UTC. Tidak ada manipulasi waktu."
    )
  )

  // === Claim Checker Data (Halaman 5-6) ===
  val sampleClaims: List<ClaimItem> = listOf(
    ClaimItem(
      id = "CLM-01",
      claimText = "Mematikan Wi-Fi sesaat sebelum karakter mati dapat mencegah pengurangan rank.",
      source = "Forum Diskusi Pemain",
      date = "2026-10-01",
      supportingEvidence = "Pemain tidak melihat animasi 'Defeat' di layar ponsel mereka saat koneksi mati.",
      opposingEvidence = "Server authoritative tetap mencatat status HP pemain menjadi 0 dan menghitung pengurangan rank di backend saat disconnect.",
      unknownAspects = "Apakah ada game spesifik yang memakai model peer-to-peer tanpa server authority.",
      verdict = ClaimVerdict.CONTRADICTED,
      analystNotes = "Pada arsitektur client-server authoritative, status kematian diputuskan oleh server. Mematikan koneksi hanya membuat client terlambat menerima status kekalahan."
    ),
    ClaimItem(
      id = "CLM-02",
      claimText = "Penggunaan macro tombol klik 50x per detik di smartphone dapat dideteksi dari analisis variansi jeda waktu.",
      source = "Developer Security Blog",
      date = "2026-09-15",
      supportingEvidence = "Jari manusia memiliki variansi motorik (jitter) 15-45ms, sedangkan macro buatan memiliki jeda identik konsisten 20.000ms tanpa variasi.",
      opposingEvidence = "Beberapa macro modern menambahkan random delay buatan.",
      unknownAspects = "Berapa lama sampel klik minimum yang dibutuhkan untuk mencapai ambang keyakinan 99%.",
      verdict = ClaimVerdict.SUPPORTED,
      analystNotes = "Didukung oleh hukum biometrik motorik manusia. Distribusi Poisson jeda klik dapat membedakan input biologis dengan timer perangkat lunak."
    ),
    ClaimItem(
      id = "CLM-03",
      claimText = "Server game X mengalami kebocoran data jutaan akun karena password disimpan tanpa enkripsi.",
      source = "Akun Media Sosial Anonim",
      date = "2026-10-05",
      supportingEvidence = "Sebuah tangkapan layar cuplikan teks notepad berisi daftar email.",
      opposingEvidence = "Tidak ada bukti cryptographic signature, dump database tidak pernah dirilis atau diverifikasi breach database resmi.",
      unknownAspects = "Asal usul daftar email tersebut (apakah daur ulang dari data breach lawas situs lain).",
      verdict = ClaimVerdict.INSUFFICIENT,
      analystNotes = "Tangkapan layar teks tanpa verifikasi struktur skema atau sampel biner resmi belum mencukupi untuk memvalidasi klaim kebocoran sistem utama."
    )
  )

  // === 6. Coding Academy Latihan (Halaman 6) ===
  val codingExercises: List<CodingExercise> = listOf(
    CodingExercise(
      id = "CODE-L1",
      levelNumber = 1,
      levelName = "Dasar",
      title = "Kalkulasi Nilai Pembelian",
      description = "Hitung total biaya pembelian aset dalam variabel total. Kalikan harga dengan jumlah unit yang dibeli.",
      initialCode = """
const harga = 10000;
const jumlah = 3;
const total = harga * jumlah;
console.log(total);
      """.trimIndent(),
      expectedOutput = "30000",
      hint = "Gunakan operator perkalian (*) antara harga dan jumlah.",
      solutionCode = """
const harga = 10000;
const jumlah = 3;
const total = harga * jumlah;
console.log(total);
      """.trimIndent()
    ),
    CodingExercise(
      id = "CODE-L2",
      levelNumber = 2,
      levelName = "HTML/CSS Logic",
      title = "Validasi Form Input Posisi",
      description = "Periksa apakah input koordinat berada dalam batas area peta permainan (0 <= x <= 1000). Tampilkan status validasi.",
      initialCode = """
const koordinatX = 450;
const mapBatasMax = 1000;

if (koordinatX >= 0 && koordinatX <= mapBatasMax) {
  console.log("KOORDINAT_VALID");
} else {
  console.log("DILUAR_MAP");
}
      """.trimIndent(),
      expectedOutput = "KOORDINAT_VALID",
      hint = "Gunakan operator logika AND (&&) untuk memeriksa batas minimum dan batas maksimum.",
      solutionCode = """
const koordinatX = 450;
const mapBatasMax = 1000;

if (koordinatX >= 0 && koordinatX <= mapBatasMax) {
  console.log("KOORDINAT_VALID");
} else {
  console.log("DILUAR_MAP");
}
      """.trimIndent()
    ),
    CodingExercise(
      id = "CODE-L3",
      levelNumber = 3,
      levelName = "JavaScript",
      title = "Pengecekan Rate Limiting (Array Filter)",
      description = "Hitung berapa banyak aksi yang dikirim dalam jendela 1000ms terakhir. Jika > 5 aksi, laporkan RATE_LIMIT_EXCEEDED.",
      initialCode = """
const timestamps = [100, 250, 400, 600, 800, 950]; // milidetik
const batasAksi = 5;

if (timestamps.length > batasAksi) {
  console.log("RATE_LIMIT_EXCEEDED");
} else {
  console.log("PERMINTAAN_DIIZINKAN");
}
      """.trimIndent(),
      expectedOutput = "RATE_LIMIT_EXCEEDED",
      hint = "Periksa panjang array timestamps dengan properti .length.",
      solutionCode = """
const timestamps = [100, 250, 400, 600, 800, 950];
const batasAksi = 5;

if (timestamps.length > batasAksi) {
  console.log("RATE_LIMIT_EXCEEDED");
} else {
  console.log("PERMINTAAN_DIIZINKAN");
}
      """.trimIndent()
    ),
    CodingExercise(
      id = "CODE-L4",
      levelNumber = 4,
      levelName = "Python",
      title = "Validasi Kinematika Jarak",
      description = "Hitung selisih jarak Euclidean antara dua titik (x1, y1) dan (x2, y2). Tampilkan apakah kecepatan melebihi 10.0 m/s.",
      initialCode = """
import math

x1, y1 = 0, 0
x2, y2 = 30, 40
delta_t = 2.0 # detik

jarak = math.sqrt((x2 - x1)**2 + (y2 - y1)**2)
kecepatan = jarak / delta_t

print(f"Jarak: {jarak}, Kecepatan: {kecepatan}")
if kecepatan > 10.0:
    print("ANOMALI_TERDETEKSI")
else:
    print("KECEPATAN_WAJAR")
      """.trimIndent(),
      expectedOutput = "Jarak: 50.0, Kecepatan: 25.0\nANOMALI_TERDETEKSI",
      hint = "Gunakan math.sqrt untuk akar kuadrat dan periksa apakah kecepatan > batas.",
      solutionCode = """
import math

x1, y1 = 0, 0
x2, y2 = 30, 40
delta_t = 2.0

jarak = math.sqrt((x2 - x1)**2 + (y2 - y1)**2)
kecepatan = jarak / delta_t

print(f"Jarak: {jarak}, Kecepatan: {kecepatan}")
if kecepatan > 10.0:
    print("ANOMALI_TERDETEKSI")
else:
    print("KECEPATAN_WAJAR")
      """.trimIndent()
    ),
    CodingExercise(
      id = "CODE-L5",
      levelNumber = 5,
      levelName = "Backend & Database",
      title = "Simulasi Transaksi Saldo Konsisten",
      description = "Pastikan saldo akun tidak bernilai negatif sebelum mengizinkan pengurangan pembelian item.",
      initialCode = """
let saldo = 5000;
const hargaItem = 6000;

if (saldo >= hargaItem) {
  saldo -= hargaItem;
  console.log("TRANSAKSI_BERHASIL: " + saldo);
} else {
  console.log("SALDO_TIDAK_MENCUKUPI");
}
      """.trimIndent(),
      expectedOutput = "SALDO_TIDAK_MENCUKUPI",
      hint = "Gunakan percabangan saldo >= hargaItem untuk mencegah nilai saldo negatif.",
      solutionCode = """
let saldo = 5000;
const hargaItem = 6000;

if (saldo >= hargaItem) {
  saldo -= hargaItem;
  console.log("TRANSAKSI_BERHASIL: " + saldo);
} else {
  console.log("SALDO_TIDAK_MENCUKUPI");
}
      """.trimIndent()
    ),
    CodingExercise(
      id = "CODE-L6",
      levelNumber = 6,
      levelName = "Lanjutan",
      title = "Verifikasi Hash Checksum Sederhana",
      description = "Bandingkan hash file laporan client dengan hash referensi resmi di server.",
      initialCode = """
const hashResmi = "e3b0c44298fc1c149afbf4c8996fb924";
const hashClient = "e3b0c44298fc1c149afbf4c8996fb924";

if (hashResmi === hashClient) {
  console.log("INTEGRITAS_TERVERIFIKASI");
} else {
  console.log("BERKAS_TELAH_DIMODIFIKASI");
}
      """.trimIndent(),
      expectedOutput = "INTEGRITAS_TERVERIFIKASI",
      hint = "Gunakan perbandingan strict equality (===) untuk memeriksa kesesuaian string hash.",
      solutionCode = """
const hashResmi = "e3b0c44298fc1c149afbf4c8996fb924";
const hashClient = "e3b0c44298fc1c149afbf4c8996fb924";

if (hashResmi === hashClient) {
  console.log("INTEGRITAS_TERVERIFIKASI");
} else {
  console.log("BERKAS_TELAH_DIMODIFIKASI");
}
      """.trimIndent()
    )
  )

  // === 7. Market Intelligence Data (Halaman 7) ===
  val cryptoAssets: List<CryptoAsset> = listOf(
    CryptoAsset(
      symbol = "BTC",
      name = "Bitcoin",
      priceUsd = 62450.00,
      change24hPercent = 2.45,
      volume24hUsd = "$28.4B",
      marketCapUsd = "$1.23T",
      circulatingSupply = "19.76M BTC",
      athUsd = 73750.00,
      atlUsd = 67.81,
      provider = "Direct Liquidity Feed Terminal",
      isRealtime = false,
      lastUpdated = "14:15 WIB (15-min cached)",
      chartPoints = listOf(60800f, 61200f, 61000f, 61900f, 62100f, 62450f)
    ),
    CryptoAsset(
      symbol = "ETH",
      name = "Ethereum",
      priceUsd = 2450.50,
      change24hPercent = -1.15,
      volume24hUsd = "$14.2B",
      marketCapUsd = "$294.8B",
      circulatingSupply = "120.3M ETH",
      athUsd = 4891.70,
      atlUsd = 0.42,
      provider = "Exchange Public Data",
      isRealtime = false,
      lastUpdated = "14:15 WIB (15-min cached)",
      chartPoints = listOf(2490f, 2470f, 2480f, 2440f, 2460f, 2450f)
    ),
    CryptoAsset(
      symbol = "SOL",
      name = "Solana",
      priceUsd = 144.20,
      change24hPercent = 4.80,
      volume24hUsd = "$3.6B",
      marketCapUsd = "$67.3B",
      circulatingSupply = "468.2M SOL",
      athUsd = 260.06,
      atlUsd = 0.50,
      provider = "Exchange Public Data",
      isRealtime = false,
      lastUpdated = "14:15 WIB (15-min cached)",
      chartPoints = listOf(136f, 138f, 140f, 141f, 143f, 144f)
    )
  )

  val stockAssets: List<StockAsset> = listOf(
    StockAsset(
      symbol = "BBCA.JK",
      name = "Bank Central Asia Tbk",
      exchange = "IDX (BEI)",
      price = 10450.0,
      currency = "IDR",
      changePercent = 0.96,
      volume = "78.4M",
      openPrice = 10350.0,
      highPrice = 10500.0,
      lowPrice = 10300.0,
      prevClose = 10350.0,
      range52w = "Rp 8,700 - Rp 10,900",
      marketCap = "Rp 1,288 T",
      peRatio = 24.2,
      eps = 431.0,
      dividendYield = 2.6,
      quoteStatus = "Delayed Quote (15-min BEI)"
    ),
    StockAsset(
      symbol = "BBRI.JK",
      name = "Bank Rakyat Indonesia Tbk",
      exchange = "IDX (BEI)",
      price = 4920.0,
      currency = "IDR",
      changePercent = -0.60,
      volume = "124.2M",
      openPrice = 4960.0,
      highPrice = 4980.0,
      lowPrice = 4900.0,
      prevClose = 4950.0,
      range52w = "Rp 4,180 - Rp 6,400",
      marketCap = "Rp 745 T",
      peRatio = 12.8,
      eps = 384.0,
      dividendYield = 6.4,
      quoteStatus = "Delayed Quote (15-min BEI)"
    ),
    StockAsset(
      symbol = "AAPL",
      name = "Apple Inc.",
      exchange = "NASDAQ",
      price = 227.50,
      currency = "USD",
      changePercent = 1.25,
      volume = "42.1M",
      openPrice = 225.00,
      highPrice = 228.30,
      lowPrice = 224.80,
      prevClose = 224.70,
      range52w = "$164.08 - $237.23",
      marketCap = "$3.46 T",
      peRatio = 34.5,
      eps = 6.59,
      dividendYield = 0.44,
      quoteStatus = "End-of-day Close Quote"
    )
  )

  // === 10. Achievements List (Halaman 9-10) ===
  val sampleAchievements: List<AchievementBadge> = listOf(
    AchievementBadge("ach-1", "First Step", "Menyelesaikan modul pembelajaran pertama", true),
    AchievementBadge("ach-2", "3-Day Streak", "Belajar 3 hari berturut-turut", true),
    AchievementBadge("ach-3", "7-Day Streak", "Belajar 7 hari berturut-turut", false),
    AchievementBadge("ach-4", "14-Day Streak", "Konsistensi belajar selama 2 minggu penuh", false),
    AchievementBadge("ach-5", "30-Day Streak", "Komitmen belajar selama 1 bulan", false),
    AchievementBadge("ach-6", "100 Active Days", "Mencatat 100 hari aktif belajar", false),
    AchievementBadge("ach-7", "First Project", "Menyelesaikan proyek coding pertama", true),
    AchievementBadge("ach-8", "First Debug", "Berhasil memperbaiki error pada latihan coding", true),
    AchievementBadge("ach-9", "First Lab Completed", "Menuntaskan eksperimen pertama di Security Lab", true)
  )

  // === 11. Tools Hub Registered Tools (Halaman 10-11) ===
  val initialToolConfigs: List<ToolConfig> = listOf(
    ToolConfig(
      id = "TOOL-01",
      name = "Local Movement Kinematic Validator",
      description = "Adapter validasi batas perpindahan karakter 2D/3D berbasis delta waktu tick.",
      category = ToolCategory.SECURITY,
      baseUrl = "internal://gamesec.lab/validator/movement",
      authType = "None (Internal Sandbox)",
      allowedEndpoints = listOf("/validate-movement", "/calculate-velocity"),
      enabled = true,
      lastHealthCheck = "Normal (0 ms)",
      isOnline = true
    ),
    ToolConfig(
      id = "TOOL-02",
      name = "Local Cooldown & Rate Limiter",
      description = "Simulasi algoritma Token Bucket dan pengecekan timestamp jeda aksi.",
      category = ToolCategory.SECURITY,
      baseUrl = "internal://gamesec.lab/validator/cooldown",
      authType = "None (Internal Sandbox)",
      allowedEndpoints = listOf("/check-cooldown", "/rate-limit"),
      enabled = true,
      lastHealthCheck = "Normal (0 ms)",
      isOnline = true
    ),
    ToolConfig(
      id = "TOOL-03",
      name = "Public Market Price Feed Adapter",
      description = "Konektor data publik crypto & saham dengan caching 15 menit dan rate-limiting.",
      category = ToolCategory.MARKET,
      baseUrl = "https://api.coingecko.com/api/v3",
      authType = "Public API (No Secret)",
      allowedEndpoints = listOf("/simple/price", "/coins/markets"),
      enabled = true,
      lastHealthCheck = "Normal (210 ms)",
      isOnline = true
    ),
    ToolConfig(
      id = "TOOL-04",
      name = "Local Python/JS Sandbox Connector",
      description = "Adapter eksekusi latihan code playground terisolasi lokal.",
      category = ToolCategory.CODING,
      baseUrl = "internal://gamesec.lab/sandbox/eval",
      authType = "None (Internal Sandbox)",
      allowedEndpoints = listOf("/run-code", "/verify-assertion"),
      enabled = true,
      lastHealthCheck = "Normal (0 ms)",
      isOnline = true
    )
  )

  // === 4. Terminal Commands Execution Handler (Halaman 5) ===
  fun executeTerminalCommand(cmd: String): String {
    val cleanCmd = cmd.trim().lowercase()
    return when {
      cleanCmd == "help" -> """
DAFTAR PERINTAH SIMULASI (GAMESEC LAB):
  help                Menampilkan daftar perintah dan contoh
  architecture        Menjelaskan pembagian client/server
  validate movement   Meminta atau memuat data gerakan latihan dan menghitung hasil
  validate cooldown   Menjalankan simulasi validasi cooldown aksi
  behavior score      Menghitung skor anomali dari dataset berlabel fiktif
  analyze logs        Mencari inkonsistensi dari log latihan forensik
  hash demo           Menjalankan perbandingan hash teks/berkas
  network demo        Menampilkan simulasi request/response dan validasi server
  rate limit demo     Menjalankan simulasi pembatasan request per detik
  security checklist  Menampilkan checklist keamanan arsitektur
  analyze claim       Membuka alur pemeriksaan klaim bukti
  history             Menampilkan ringkasan sesi terminal
  status              Status sesi simulasi sandbox
  clear               Membersihkan layar terminal
      """.trimIndent()

      cleanCmd == "architecture" -> """
[CLIENT-SERVER RESPONSIBILITY MATRIX]
-------------------------------------------------------------
LAYER           CLIENT RESPONSIBILITY     SERVER RESPONSIBILITY
-------------------------------------------------------------
Presentation    Mesh render, Animations   None (Headless Server)
Input           Capture keys / touch      Validate sequence & bounds
State           Temporary local estimate  Authoritative truth
Physics         Prediction smoothing      Collision & Bounds check
Economy         Display inventory balance Final transaction ledger
Rules           Read-only visual timers   Enforce cooldowns & victory
-------------------------------------------------------------
PRINSIP: Server adalah sumber kebenaran. Client hanya pengirim niat aksi.
      """.trimIndent()

      cleanCmd == "validate movement" -> """
[SIMULASI: SERVER MOVEMENT VALIDATION PIPELINE]
Tick ID         : #49281
Timestamp Delta : 0.050s (50 ms)
Posisi Lama     : (x: 104.20, y: 55.10, z: 12.00)
Posisi Laporan  : (x: 119.50, y: 55.10, z: 12.00)
Jarak Tempuh    : 15.30 meter

Kalkulasi Kecepatan:
  Kecepatan Terhitung : 15.30 m / 0.050 s = 306.00 m/s
  Batas Karakter      : 8.50 m/s (Sprint Buff: 11.05 m/s)
  Batas Maksimum Izinkan : 11.05 m/s * 0.050 s * 1.15 (toleransi) = 0.635 meter

HASIL VALIDASI:
  [!] STATUS: MELAMPAUI BATAS (REJECT & ROLLBACK)
  Alasan        : Jarak (15.30m) > Batas Toleransi (0.635m)
  Tindakan      : Rollback posisi ke (x: 104.20, y: 55.10, z: 12.00)
  Log Flag      : SuspiciousSpeedDelta (+15 risk pts)
      """.trimIndent()

      cleanCmd == "validate cooldown" -> """
[SIMULASI: VALIDASI COOLDOWN SKILL]
Skill ID         : SKILL_LIGHTNING_BOLT
Minimum Cooldown : 4.000 detik
Waktu Aksi Lalu  : 14:00:10.100 UTC
Waktu Aksi Baru  : 14:00:12.350 UTC
Selisih Waktu    : 2.250 detik

HASIL VALIDASI:
  [!] KEPUTUSAN : DITOLAK (COOLDOWN VIOLATION)
  Alasan        : Aksi dieksekusi 1.750 detik terlalu cepat sebelum cooldown berakhir.
  Status Server : Packet Dropped, client UI disinkronkan kembali.
      """.trimIndent()

      cleanCmd == "behavior score" -> """
[SIMULASI: BEHAVIOR ANOMALY SCORING]
Target Sesi       : Player_Session_#8841
Metrik Diamati    : Input Frequency & Precision Variance

1. Frekuensi Input Sentuhan:
   - Nilai Rata-rata : 24.8 clicks/detik
   - Deviasi Standar : 0.001 ms (Pola input seragam absolut)
   - Batas Biologis  : Deviasi < 5 ms sangat jarang pada manusia

2. Sudut Rotasi Aim:
   - Waktu Transisi  : 0 ms (Perpindahan sudut 180 derajat seketika)
   - Kurva Gerak     : Garis lurus diskrit tanpa akselerasi/deselerasi

SKOR RISIKO:
  [+] Jitter Variance Anomaly : +40 pts
  [+] Snap Angle Telemetry    : +35 pts
  Total Risk Score            : 75 / 100
  Status Evaluasi             : PERLU PEMERIKSAAN (Ditandai untuk peninjauan)
      """.trimIndent()

      cleanCmd == "analyze logs" -> """
[SIMULASI: LOG INCONSISTENCY ANALYZER]
Memindai dataset sesi #4019...
- Total 6 baris rekaman dianalisis.
- Ditemukan 2 anomali:
  1. [14:02:01.220 UTC] Delta jarak 102m dalam 50ms (Teleport anomaly).
  2. [14:02:01.550 UTC] Tembakan dilaporkan sebelum cooldown selesai.
Kesimpulan Sementara: Sesi menunjukkan indikasi desinkronisasi atau modifikasi client.
      """.trimIndent()

      cleanCmd == "hash demo" -> """
[SIMULASI: HASH COMPARISON DEMO]
Input Teks 1 : "GameAsset_CharacterMesh_v1.0"
SHA-256 (1)  : 3a7bd3e2360a3d29eea436fcfb7e44c735d117c42d1c1835420b6b9942dd4f1b

Input Teks 2 : "GameAsset_CharacterMesh_v1.1" (perubahan 1 karakter)
SHA-256 (2)  : f1e2c3b4a5d6e7f8091a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e

Kesesuaian   : TIDAK COCOK (0% Bitwise Match)
Catatan      : Hash menjamin integritas data, tetapi bukan bukti menyeluruh bahwa proses aman.
      """.trimIndent()

      cleanCmd == "network demo" -> """
[SIMULASI: REQUEST / RESPONSE SERVER VALIDATION]
Client Request Payload:
{
  "client_seq": 1042,
  "client_ammo_claimed": 999,
  "action": "RELOAD_WEAPON"
}

Server Verification Check:
- Amunisi Resmi di Server Ledger : 30 peluru
- Selisih Klaim vs Resmi         : +969 peluru tidak sah
Server Response:
{
  "status": 403,
  "error": "INVENTORY_MISMATCH",
  "authoritative_ammo": 30
}
      """.trimIndent()

      cleanCmd == "rate limit demo" -> """
[SIMULASI: RATE LIMITING DEMO (SLIDING WINDOW)]
Ambang Batas : 5 permintaan per 1 detik (1000 ms)
Simulasi Pengiriman 8 permintaan seketika:
- Req #1: [200 OK] Diterima
- Req #2: [200 OK] Diterima
- Req #3: [200 OK] Diterima
- Req #4: [200 OK] Diterima
- Req #5: [200 OK] Diterima
- Req #6: [429 TOO_MANY_REQUESTS] Ditolak (Limit tercapai)
- Req #7: [429 TOO_MANY_REQUESTS] Ditolak
- Req #8: [429 TOO_MANY_REQUESTS] Ditolak
      """.trimIndent()

      cleanCmd == "security checklist" -> """
[SECURITY ARCHITECTURE CHECKLIST]
[x] 1. Authoritative Server diaktifkan untuk status posisi & HP
[x] 2. Transaksi ekonomi menggunakan database ACID dengan row locking
[x] 3. Rate limiting diterapkan di gerbang API jaringan
[x] 4. Serialisasi paket menyertakan nomor urut (Sequence number)
[x] 5. Rahasia API (secrets) disimpan di backend, tidak di frontend
[x] 6. Jam lokal client tidak dijadikan acuan cooldown server
      """.trimIndent()

      cleanCmd == "analyze claim" -> """
[SIMULASI: ALUR PEMERIKSAAN KLAIM BUKTI]
1. Baca teks klaim dan identifikasi entitas pembuat klaim.
2. Periksa sumber asli: apakah independen atau memiliki konflik kepentingan?
3. Cek bukti pendukung vs bukti yang bertentangan.
4. Identifikasi penjelasan alternatif (contoh: lag jaringan vs cheat sengaja).
5. Tentukan kategori: Didukung bukti, Bertentangan, Belum cukup bukti, atau Tidak dapat diverifikasi.
      """.trimIndent()

      cleanCmd == "history" -> """
[RIWAYAT SESI TERMINAL]
Sesi aktif. Gunakan tombol chip perintah cepat atau ketik perintah di bawah.
      """.trimIndent()

      cleanCmd == "status" -> """
[GAMESEC LAB SIMULATION SANDBOX STATUS]
Host Sandbox       : Local Conceptual & Mathematical Simulator
Access Mode        : Isolated (No External Game Hooking)
Active Modules     : 10 Core Modules Loaded
Validation Engine  : Movement, Cooldown, Rate-Limit, Hash, & Behavioral Heuristics Ready
      """.trimIndent()

      cleanCmd.isEmpty() -> ""

      else -> "Perintah '$cmd' tidak dikenal. Ketik 'help' untuk melihat daftar perintah dan contoh."
    }
  }

  // Quiz questions
  val quizQuestions: List<QuizQuestion> = listOf(
    QuizQuestion(
      id = 1,
      question = "Mengapa server disebut sebagai sumber kebenaran (authoritative source)?",
      options = listOf(
        "Karena server berjalan di mesin terisolasi yang dikontrol developer, sehingga keabsahan status permainan dapat divalidasi tanpa campur tangan pengguna.",
        "Karena server memiliki spesifikasi perangkat keras grafik yang lebih tinggi daripada perangkat pemain.",
        "Karena server dapat merender animasi secara lebih cepat dibandingkan smartphone pemain.",
        "Karena koneksi server tidak pernah mengalami packet loss atau latency."
      ),
      correctIndex = 0,
      explanation = "Server disebut sumber kebenaran karena berada di infrastruktur yang dikendalikan developer secara independen. Keputusan status permainan (HP, posisi resmi, transaksi) ditentukan di server sehingga meminimalkan celah manipulasi lokal."
    ),
    QuizQuestion(
      id = 2,
      question = "Apa risiko utama dari penerapan client-side prediction?",
      options = listOf(
        "Membuat konsumsi baterai smartphone menjadi lebih hemat.",
        "Membuka celah bagi client untuk memanipulasi posisi atau kecepatan jika server tidak memverifikasi ulang hasil prediksi secara ketat.",
        "Menghilangkan kebutuhan akan koneksi internet pada game multiplayer.",
        "Mengharuskan grafis game diturunkan ke resolusi rendah."
      ),
      correctIndex = 1,
      explanation = "Client-side prediction memungkinkan perangkat pemain menghitung pergerakan lokal sebelum dikonfirmasi server. Jika server mempercayai koordinat laporan client tanpa validasi jarak per waktu, client dapat merekayasa posisi secara sepihak."
    ),
    QuizQuestion(
      id = 3,
      question = "Apa perbedaan mendasar antara validasi server-side dan analisis perilaku (behavioral analysis)?",
      options = listOf(
        "Validasi server-side mengecek logika fisika aksi seketika, sedangkan analisis perilaku mengamati pola dan distribusi data pemain dari waktu ke waktu.",
        "Validasi server-side hanya untuk grafis, sedangkan analisis perilaku untuk suara game.",
        "Analisis perilaku dilakukan di memori RAM client, sedangkan validasi server-side tidak menggunakan kode apapun.",
        "Validasi server-side bersifat acak, sedangkan analisis perilaku bersifat statis."
      ),
      correctIndex = 0,
      explanation = "Validasi server-side memeriksa keabsahan aturan permainan secara langsung per kejadian (misal: jarak <= kecepatan * waktu). Analisis perilaku mengevaluasi data jangka panjang (seperti konsistensi aim, variasi jeda klik, dan distribusi statistik) untuk mendeteksi anomali."
    ),
    QuizQuestion(
      id = 4,
      question = "Mengapa mekanisme rate limiting sangat penting dalam komunikasi jaringan game?",
      options = listOf(
        "Untuk mencegah pemain memenangkan pertandingan terlalu cepat.",
        "Untuk membatasi frekuensi aksi per satuan waktu, sehingga mencegah banjir permintaan otomatis (spam paket/bot) yang tidak wajar.",
        "Untuk mempercepat waktu download pembaruan game.",
        "Untuk mengubah tampilan UI antarmuka pemain."
      ),
      correctIndex = 1,
      explanation = "Rate limiting memastikan pemain tidak dapat mengeksekusi aksi melebihi batas manusiawi atau aturan desain (misal: membatasi request tembakan per detik), sekaligus melindungi server dari overload request paket jaringan."
    ),
    QuizQuestion(
      id = 5,
      question = "Apa yang terjadi jika logika cooldown skill mengandalkan jam internal (system clock) perangkat pemain?",
      options = listOf(
        "Cooldown akan selalu sinkron dengan server secara otomatis.",
        "Pemain dapat memanipulasi waktu lokal perangkat untuk melewati masa tunggu cooldown secara instan jika server tidak memvalidasi timestamp.",
        "Grafik game akan berjalan dua kali lebih cepat.",
        "Koneksi internet game akan langsung terputus permanen."
      ),
      correctIndex = 1,
      explanation = "Jam perangkat lokal sepenuhnya berada di bawah kendali pengguna. Jika game tidak menggunakan waktu terpusat server (server tick/epoch), mengubah jam sistem HP dapat memicu manipulasi timer cooldown."
    ),
    QuizQuestion(
      id = 6,
      question = "Mengapa enkripsi paket jaringan saja belum cukup untuk menjamin integritas data game?",
      options = listOf(
        "Karena enkripsi membuat ukuran file game menjadi terlalu besar.",
        "Karena manipulasi dapat dilakukan di memori internal client sebelum data tersebut dienkripsi dan dikirim ke lapisan jaringan.",
        "Karena enkripsi hanya bekerja pada koneksi Wi-Fi.",
        "Karena enkripsi tidak didukung oleh arsitektur client-server."
      ),
      correctIndex = 1,
      explanation = "Enkripsi melindungi data dari penyadapan saat transit di jaringan (Man-in-the-Middle). Namun jika proses client itu sendiri dimanipulasi di RAM, data yang dienkripsi dan dikirim adalah data yang sudah tidak valid sejak awal."
    ),
    QuizQuestion(
      id = 7,
      question = "Faktor apa yang menjadi trade-off utama sehingga tidak seluruh logika fisika game dipindahkan ke server?",
      options = listOf(
        "Latency jaringan yang mengorbankan kelancaran respon kontrol pemain, serta biaya komputasi infrastruktur server yang sangat tinggi.",
        "Kurangnya minat pemain terhadap grafik beresolusi tinggi.",
        "Ketidakmampuan bahasa pemrograman modern menjalankan kalkulasi fisika.",
        "Aturan hak cipta perangkat keras komputer."
      ),
      correctIndex = 0,
      explanation = "Memindahkan 100% komputasi ke server akan membuat setiap input kontrol pemain bergantung pada waktu tempuh sinyal jaringan (latency), membuat game terasa kaku/lambat, serta menuntut kapasitas komputasi server yang sangat mahal."
    ),
    QuizQuestion(
      id = 8,
      question = "Bagaimana memory editing beroperasi pada perangkat pemain?",
      options = listOf(
        "Dengan mengganti motherboard perangkat keras secara fisik saat bermain.",
        "Dengan memindai dan menimpa variabel nilai yang tersimpan di alokasi RAM proses game yang sedang berjalan.",
        "Dengan memblokir sinyal satelit GPS pada smartphone.",
        "Dengan mengirim email otomatis ke developer game."
      ),
      correctIndex = 1,
      explanation = "Memory editing memanfaatkan akses debugger atau proses root untuk mencari alamat memori lokal (misal koordinat X/Y atau nilai amunisi lokal) dan menulis ulang nilainya saat aplikasi game sedang dieksekusi di RAM."
    )
  )

  val detectionSteps: List<DetectionStep> = listOf(
    DetectionStep(
      stepNumber = 1,
      title = "Client Mengirim Data Posisi dan Timestamp",
      formula = "Payload: { player_id, pos_x, pos_y, timestamp_client }",
      explanation = "Ketika pemain bergerak, client menghitung posisi lokal sementara lalu membungkusnya dalam paket jaringan UDP/TCP bersama catatan waktu pengiriman.",
      technicalNote = "Timestamp client tidak boleh dipercaya mentah-mentah; server menggunakan timestamp penerimaan internal sebagai patokan pembanding."
    ),
    DetectionStep(
      stepNumber = 2,
      title = "Server Menghitung Jarak Tempuh",
      formula = "distance = sqrt( (pos_x2 - pos_x1)^2 + (pos_y2 - pos_y1)^2 )",
      explanation = "Server mengambil posisi resmi terakhir yang tercatat di basis data sesi, lalu menghitung jarak euklidean Euclidean terhadap posisi baru yang dilaporkan.",
      technicalNote = "Pada game 3D, ketinggian sumbu Z juga dihitung dengan memperhitungkan akselerasi jatuh gravitasi."
    ),
    DetectionStep(
      stepNumber = 3,
      title = "Server Membandingkan Jarak dengan Kecepatan Maksimum",
      formula = "max_allowed = (v_max * delta_t) * (1.0 + latency_tolerance)",
      explanation = "Server menghitung jarak teoritis terjauh yang mungkin dicapai karakter selama selisih waktu delta_t, ditambah margin toleransi jitter jaringan.",
      technicalNote = "Jika selisih waktu terlalu kecil namun jarak terlampau besar, rasio kecepatan seketika akan melonjak."
    ),
    DetectionStep(
      stepNumber = 4,
      title = "Penandaan Anomali (Flagging)",
      formula = "if (distance > max_allowed) -> FLAG: SUSPICIOUS_MOVEMENT",
      explanation = "Jika jarak tempuh melampaui ambang batas toleransi, paket tersebut tidak langsung memicu penolakan fatal melainkan diberi label anomali.",
      technicalNote = "Pemberian label awal menghindari false-positive akibat lonjakan lag jaringan (packet loss burst) yang dialami pemain normal."
    ),
    DetectionStep(
      stepNumber = 5,
      title = "Eskalasi Skor Risiko Berulang",
      formula = "risk_score += (distance - max_allowed) * weight_factor",
      explanation = "Jika anomali yang sama terjadi berulang kali dalam jendela waktu tertentu, server meningkatkan skor risiko akumulatif akun pemain.",
      technicalNote = "Algoritma sliding window digunakan untuk meluruhkan skor secara bertahap jika gerakan kembali normal."
    ),
    DetectionStep(
      stepNumber = 6,
      title = "Penerapan Tindakan Terukur",
      formula = "if (risk_score >= THRESHOLD_WARN) -> Teleport Rollback\nif (risk_score >= THRESHOLD_FLAG) -> Flag for Review",
      explanation = "Bila skor melewati ambang batas toleransi, server mengeksekusi mitigasi: mengoreksi posisi karakter kembali ke titik resmi terakhir (server rollback), membatasi sesi, atau mengirim log ke tim investigasi.",
      technicalNote = "Tindakan server bersifat bertahap untuk menjaga stabilitas sesi permainan secara keseluruhan."
    )
  )

  val references: List<ReferenceItem> = listOf(
    ReferenceItem(
      id = "ref-1",
      title = "Client-Server Architecture in Multiplayer Games",
      summary = "Pemisahan model komputasi antara presentasi antarmuka di client dan simulasi state berwibawa di host terpusat.",
      keyTerms = listOf("Authoritative Server", "Dumb Terminal", "State Synchronization", "Network Tick Rate"),
      industrialPractice = "Standar industri multiplayer modern menetapkan bahwa client hanya menjadi presenter visual dan pemancar input intent, sedangkan dunia virtual disimulasikan secara deterministik di server."
    ),
    ReferenceItem(
      id = "ref-2",
      title = "Server Authority & Client Prediction",
      summary = "Teknik rekonsiliasi state di mana client memprediksi respon gerak seketika tanpa menunggu paket balik server, dengan mekanisme rekonsiliasi saat terjadi selisih.",
      keyTerms = listOf("Dead Reckoning", "Client-Side Prediction", "Server Reconciliation", "Desync Rollback"),
      industrialPractice = "Digunakan pada game berkecepatan tinggi (FPS, MOBA) untuk mengatasi jeda waktu round-trip time (RTT). Memerlukan batas toleransi kesalahan yang ketat agar tidak disalahgunakan."
    ),
    ReferenceItem(
      id = "ref-3",
      title = "Movement Validation Techniques",
      summary = "Algoritma verifikasi jarak dan posisi berbasis kinematika fisika per selang waktu waktu tick server.",
      keyTerms = listOf("Delta Time Checking", "Velocity Clamping", "Raycasting Collision", "Latency Buffer"),
      industrialPractice = "Server memverifikasi formula v = delta_s / delta_t. Apabila hasil perhitungan melebihi kapasitas kemampuan karakter ditambah buffer jitter, posisi dikoreksi paksa (rollback)."
    ),
    ReferenceItem(
      id = "ref-4",
      title = "Behavioral Anti-Cheat & Heuristics",
      summary = "Pemodelan statistik dan pembelajaran mesin untuk mendeteksi anomali perilaku input yang tidak wajar.",
      keyTerms = listOf("Aim Inconsistency", "Reaction Time Threshold", "Input Variance", "Risk Scoring"),
      industrialPractice = "Menganalisis pola distribusi input analog/sentuhan manusia versus pola buatan (bot) yang memiliki presisi piksel nol-variansi atau kecepatan reaksi di bawah batas biologis manusia."
    ),
    ReferenceItem(
      id = "ref-5",
      title = "Integrity Checking Concepts",
      summary = "Pemeriksaan konsistensi kode biner, struktur memori, dan aset permainan dari modifikasi pihak ketiga.",
      keyTerms = listOf("Cryptographic Hashing", "Code Signing", "Memory Hook Detection", "Kernel-level vs User-space"),
      industrialPractice = "Verifikasi tanda tangan digital berkas game sebelum dieksekusi. Digunakan sebagai garis pertahanan pertama untuk mendeteksi injeksi pustaka luar (DLL injection)."
    )
  )
}
