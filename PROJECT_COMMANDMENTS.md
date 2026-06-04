# GL027 SMSColorGreen - Điều Răn Làm Dự Án

Dự án này là app SMS/MMS có thể đọc, gửi tin nhắn, gửi hình ảnh và tùy biến màu sắc, background, font chữ, chủ đề. Khi làm việc trong repo này, ưu tiên chất lượng của một app nhắn tin thật: ổn định, riêng tư, nhanh và dễ đổi giao diện.

## 1. Không coi đây là template nữa

Mọi code mới phải phục vụ sản phẩm SMS customization. Không copy placeholder từ `activity_main.xml` hoặc tài liệu template cũ nếu nó không hợp với inbox, conversation, composer, media hoặc theme.

## 2. Base class là luật cứng

- Activity phải extends `BaseActivity<VB>` và dùng `Binding::inflate`.
- Fragment phải extends `BaseFragment` hoặc `BaseBottomFragment`.
- Dialog phải extends `BaseDialog`.
- RecyclerView adapter phải dựa trên base adapter sẵn có; danh sách chat/inbox nên ưu tiên `BaseListAdapter` + `DiffUtil`.

## 3. ViewBinding, không `findViewById`

Mọi màn hình dùng XML layout riêng và ViewBinding. Không thêm `findViewById`, không dựng UI phức tạp bằng code nếu XML làm tốt hơn.

## 4. UI XML phải sạch

TextView/Button/EditText phải dùng style. Button, row, icon hoặc vùng clickable phải có ripple. Nếu chạm vào layout cũ đang hardcode `textSize`, `textColor`, `textStyle`, hãy sửa phần đang chạm.

## 5. SMS permission không đồng nghĩa default SMS app

Luôn tách 2 việc: runtime permission và default SMS role. Muốn ghi/gửi/quản lý SMS đúng chuẩn phải kiểm tra `PermissionUtils.isDefaultSmsApp()` và request role bằng flow phù hợp Android Q+ / pre-Q.

## 6. MMS và hình ảnh là feature riêng

Không xử lý ảnh như text message mở rộng. Phải có model attachment, content type, uri, size, trạng thái gửi/tải, lỗi thất bại và UI preview. Không log uri nhạy cảm hoặc đường dẫn file thật.

## 7. Tin nhắn là dữ liệu riêng tư

Không log nội dung tin nhắn, số điện thoại, contact đầy đủ, attachment uri, hoặc payload MMS. Debug log chỉ được ghi trạng thái, số lượng, lỗi đã mask.

## 8. Không query SMS/MMS trên main thread

Đọc provider, gửi SMS/MMS, decode ảnh, load contact và Room query phải chạy IO/background. UI chỉ observe state đã chuẩn bị.

## 9. Theme phải có nguồn sự thật duy nhất

Màu, background, font, bubble shape, wallpaper và active theme phải được model hóa. Không rải raw color/font khắp Activity/Adapter. Dùng Room cho theme cần quản lý danh sách; dùng `SpManager` cho lựa chọn nhỏ như active theme id.

## 10. Chat UI phải chịu được dữ liệu lớn

Inbox/conversation phải dùng pagination hoặc windowed loading, `DiffUtil`, thumbnail ảnh, cache bằng Glide, stable state khi rotate/resume, và không `notifyDataSetChanged()` bừa bãi cho luồng chat lớn.

## 11. Clean Architecture trước khi nhanh tay

Provider/network/storage nằm ở data layer. Model UI nằm ở domain. UseCase chứa nghiệp vụ. Activity/Fragment chỉ điều phối UI, permission, navigation và observe ViewModel.

## 12. Manifest là hợp đồng hệ thống

Trước khi làm default SMS đầy đủ, kiểm tra manifest có đủ receiver/activity/service Android yêu cầu: SMS deliver, MMS WAP push, SENDTO/SMSTO compose, respond-via-message nếu cần. Không chỉ thêm permission rồi xem như xong.

## 13. Xử lý edge case của điện thoại thật

Luôn nghĩ tới dual SIM, máy không có telephony, mất default SMS role, quyền bị revoke, airplane mode, số nhận không hợp lệ, SMS dài bị chia segment, MMS quá dung lượng, gửi thất bại và app bị kill giữa chừng.

## 14. Resource phải sống trong resource

Text vào `strings.xml`, kích thước tái sử dụng vào `dimens.xml`, màu/theme vào `colors.xml` hoặc token/theme model. Không hardcode nhiều nơi.

## 15. Build/lint trước khi giao

Sau thay đổi code Android, tối thiểu chạy `gradlew.bat compileDebugKotlin`. Nếu đụng XML/resource/permission/theme, chạy thêm `gradlew.bat lintDebug` khi có thể.
