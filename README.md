# Hytur Tu Tiên 3.0.0 — Ngự Kiếm Phi Hành & Thiên Kiếm Giáng Thế

Dự án mod Minecraft **Fabric 1.20.1 / Java 17** bổ sung 10 cảnh giới × 10 tầng vào HyturCraft. Sử dụng chung mod ID **`hyturtutien`** và dữ liệu NBT của bản 1.x / 2.x để giữ cảnh giới khi cập nhật.

## Điểm mới 3.0

**Ngự Kiếm Phi Hành** — Nhấn đôi phím nhảy (Space mặc định) để đứng trên mô hình kiếm 3D (item display) phát sáng và bay tự do như Creative. Kiếm được triệu hồi bên dưới chân, theo vị trí/hướng nhìn và tỏa linh khí theo màu cảnh giới, không cần Elytra và không chiếm ô trang bị. Có **10 kiểu màu**, theo 10 đại cảnh giới. Đây là kiếm hiển thị theo người chơi, không phải phương tiện có hitbox để đứng lên khi không bay.

- Trúc Cơ: 15 giây + 0,5 giây/tầng thêm.
- Kim Đan: 20 giây + 0,5 giây/tầng thêm.
- Nguyên Anh: 25 giây + 0,5 giây/tầng thêm.
- Hóa Thần: 35 giây + 0,5 giây/tầng thêm.
- Luyện Hư: 50 giây + 0,5 giây/tầng thêm.
- Hợp Thể: 80 giây + 0,5 giây/tầng thêm.
- Đại Thừa, Độ Kiếp, Chân Tiên: bay vô hạn.

**Xé Rách Hư Không · Thiên Kiếm Giáng Thế** — Nhấn **I** từ **Hợp Thể** trở lên để chọn vị trí trong tầm 36 block; mở khe hư không bằng các vòng particle hồng tím, điện quang và phù văn; sau đó triệu hồi **mô hình đại kiếm 3D** từ trên cao lao xuống. Khi chạm đất, sóng xung kích và **trận pháp nhiều lớp** lan ra, gây sát thương diện rộng và đẩy lùi quái thù địch. Không phá địa hình, không đánh trúng người chơi khác hoặc thú nuôi. Có hồi chiêu.

| Cảnh giới | Bán kính | Hồi chiêu |
|---|---:|---:|
| Hợp Thể | 10 block | 85 giây |
| Đại Thừa | 12 block | 70 giây |
| Độ Kiếp | 14 block | 60 giây |
| Chân Tiên | 16 block | 45 giây |

Có thể chỉnh `ultimateDamageMultiplier` và `ultimateDamageCap` trong `config/hyturtutien.json` để cân bằng với Boss của modpack. Sát thương mặc định khoảng 77–106 (giảm theo khoảng cách, tối đa 120) và **không xuyên miễn thương tùy mod Boss**.

## Điều khiển

- **H:** Phóng Kiếm (Luyện Khí–Kim Đan), Vạn Kiếm Quy Tông (Nguyên Anh trở lên; bấm 2 lần để tụ/phóng).
- **I:** Sát chiêu Xé Rách Hư Không · Thiên Kiếm Giáng Thế (Hợp Thể trở lên).
- **Z:** Thuấn Di (Hóa Thần trở lên).
- **Nhấn đôi Space:** Bật/tắt Ngự Kiếm Phi Hành (Trúc Cơ trở lên).
- **Không có phím X hoặc bế quan.** Đủ linh khí là tự đột phá; **R** của Dash HyturCraft không bị sử dụng.

## Các tính năng được giữ lại

- 10 đại cảnh giới × 10 tầng, tăng máu, sát thương, giáp và tốc độ.
- Tu vi từ đánh quái và 2 đan tăng linh khí; tự động thăng tầng ngay khi đủ.
- Aura màu riêng, pháp luân nhiều lớp từ Nguyên Anh; hiệu ứng sét không phá block, trận pháp và linh long từ hạt sáng khi đột phá.
- 4 đan: **Tụ Linh Đan, Hồi Nguyên Đan, Hộ Thể Đan, Thiên Linh Đan** có thể có trong chest loot từ mod khác qua loot table chuẩn. Loot tự cấp qua code riêng có thể không được hỗ trợ.
- Lưu cảnh giới vào dữ liệu player; không mất khi chết/đăng nhập.

## Build qua GitHub Actions

**Đây là dự án mã nguồn ZIP, chưa phải file JAR đã biên dịch/kiểm thử trong game.**

1. Tải hết các file và thư mục của ZIP lên repo GitHub mới hoặc cập nhật repo cũ, bảo đảm **`src/` đầy đủ**. Dùng mã nguồn 3.0 thay thế mã nguồn 2.x.
2. Nếu GitHub chặn tải thư mục `.github`, hãy giữ workflow của repo cũ, hoặc tạo trực tiếp file `.github/workflows/build.yml` theo nội dung đã có trong ZIP.
3. Vào **Actions → Build Hytur Tu Tien 3.0 → Run workflow**. Khi dấu tích xanh, tải ZIP ở phần **Artifacts**.
4. Giải nén Artifacts, chọn **`hytur-tu-tien-3.0.0.jar`** (không chọn `-sources.jar`). Xóa JAR Tu Tiên cũ trong thư mục `mods`, thay bằng file mới.
5. Cài ở **cả server và client** Fabric 1.20.1, yêu cầu Fabric API và Java 17+. Sao lưu world trước khi thử.

Nếu sử dụng repo cũ trên GitHub: trang Upload files không tự xóa các file `.java` cũ không còn dùng nữa; cần xóa các file mixin Elytra cũ và `FlightPoseHolder.java` nếu vẫn còn trong repo. Workflow chính xác của ZIP sẽ nhận file `.jar` bản 3.0.0.

## Lệnh kiểm tra (cần OP để chỉnh tầng)

- `/canhgioi` xem tiến độ.
- `/canhgioi settang 21` → Kim Đan tầng 1 để thử ngự kiếm.
- `/canhgioi settang 61` → Hợp Thể tầng 1 để thử sát chiêu phím I.
- `/canhgioi addxp 10000` → thêm linh khí.
- `/canhgioi reload` → đọc lại config.

## Giới hạn đồ họa và tương thích

- Kiếm được dựng bằng **mô hình item 3D tùy chỉnh và particle Minecraft 1.20.1**, không có shader nứt màn hình, volumetric lighting hay mô hình rồng HD như phim/video. Có thể kết hợp shader có sẵn trong modpack để tăng độ đẹp.
- Bay dùng `PlayerAbilities` nên có khả năng tương tác khác thường với mod bay/lướt của HyturCraft. Khi bay, nhân vật đứng, không nằm ngang kiểu Elytra như bản 2.0.
- Mỗi kỹ năng phải được thử trực tiếp với phiên bản HyturCraft đang dùng. Nếu build lỗi, xem log GitHub Actions. Mã nguồn chưa được biên dịch trong môi trường biên soạn này vì không tải được Gradle/Fabric qua mạng.
