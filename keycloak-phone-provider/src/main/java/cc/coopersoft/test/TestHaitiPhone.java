package cc.coopersoft.test;

import com.google.i18n.phonenumbers.*;

public class TestHaitiPhone {
    public static void main(String[] args) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String regionCode = "HT"; // Haiti

        // Test các số điện thoại với prefix 58
        String[] testNumbers = {
            "+50958000040",   // Số có prefix 58 với mã quốc gia
            "58000040",       // Số có prefix 58 không có mã quốc gia
            "+50956000079",   // Số khác để so sánh (prefix 22)
            "+50928123456",   // Số khác để so sánh (prefix 28)
            "+50929123456",   // Số khác để so sánh (prefix 29)
            "+50934123456",   // Số khác để so sánh (prefix 34)
            "+50936123456",   // Số khác để so sánh (prefix 36)
            "+50937123456",   // Số khác để so sánh (prefix 37)
            "+50938123456"    // Số khác để so sánh (prefix 38)
        };

        System.out.println("Testing phone numbers for Haiti (HT):");
        System.out.println("Country code: +" + phoneUtil.getCountryCodeForRegion(regionCode));
        System.out.println("=========================================");

        for (String number : testNumbers) {
            try {
                Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, regionCode);
                boolean isValid = phoneUtil.isValidNumber(phoneNumber);
                boolean isPossible = phoneUtil.isPossibleNumber(phoneNumber);
                PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);

                System.out.println("\nSố điện thoại: " + number);
                System.out.println("  - Valid: " + isValid);
                System.out.println("  - Possible: " + isPossible);
                System.out.println("  - Type: " + numberType);
                System.out.println("  - E164 format: " + phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164));
                System.out.println("  - National format: " + phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));

            } catch (NumberParseException e) {
                System.out.println("\nSố điện thoại: " + number);
                System.out.println("  - ERROR: " + e.getMessage());
            }
        }

        // Lấy thông tin về các prefix được hỗ trợ
        System.out.println("\n=========================================");
        System.out.println("Supported prefixes for Haiti mobile numbers:");
        System.out.println("Thư viện libphonenumber sử dụng metadata để xác định");
        System.out.println("các prefix hợp lệ cho từng quốc gia.");
    }
}
