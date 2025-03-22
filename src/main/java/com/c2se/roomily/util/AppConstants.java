package com.c2se.roomily.util;

import java.util.List;

public class AppConstants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIR = "asc";
    public static final Integer VALID_REPORT_THRESHOLD = 10;
    public static final Integer DEBT_DATE_THRESHOLD = 7;
    public static final Double MIN_WEIGHT = 0.7;
    public static final String CONTRACT_TEMPLATE_PATH = "src/main/resources/static/contract.html";
    public static final Integer DEPOSIT_PAYMENT_TIMEOUT = 12*60;
    public static final List<String> DEFAULT_RESPONSIBILITIES_A = List.of(
            "- Tạo mọi điều kiện thuận lợi để bên B thực hiện theo hợp đồng.",
            "- Cung cấp nguồn điện, nước, wifi cho bên B sử dụng.");
    public static final List<String> DEFAULT_RESPONSIBILITIES_B = List.of(
            "- Thanh toán đầy đủ các khoản tiền theo đúng thỏa thuận.",
            "- Bảo quản các trang thiết bị và cơ sở vật chất của bên A trang bị cho ban đầu (làm hỏng phải sửa, mất phải đền).",
            "- Không được tự ý sửa chữa, cải tạo cơ sở vật chất khi chưa được sự đồng ý của bên A.",
            "- Giữ gìn vệ sinh trong và ngoài khuôn viên của phòng trọ.",
            "- Bên B phải chấp hành mọi quy định của pháp luật Nhà nước và quy định của địa phương.",
            "- Nếu bên B cho khách ở qua đêm thì phải báo và được sự đồng ý của chủ nhà đồng thời phải chịu trách nhiệm về các hành vi vi phạm pháp luật của khách trong thời gian ở lại.");
    public static final List<String> DEFAULT_RESPONSIBILITIES_COMMON = List.of(
            "- Hai bên phải tạo điều kiện cho nhau thực hiện hợp đồng.",
            "- Trong thời gian hợp đồng còn hiệu lực nếu bên nào vi phạm các điều khoản đã thỏa thuận thì bên còn lại có quyền đơn phương chấm dứt hợp đồng</a>; nếu sự vi phạm hợp đồng đó gây tổn thất cho bên bị vi phạm hợp đồng thì bên vi phạm hợp đồng phải bồi thường thiệt hại.",
            "- Một trong hai bên muốn chấm dứt hợp đồng trước thời hạn thì phải báo trước cho bên kia ít nhất 30 ngày và hai bên phải có sự thống nhất.",
            "- Bên A phải trả lại tiền đặt cọc cho bên B.",
            "- Bên nào vi phạm điều khoản chung thì phải chịu trách nhiệm trước pháp luật.",
            "- Hợp đồng được lập thành 02 bản có giá trị pháp lý như nhau, mỗi bên giữ một bản."
    );

}
