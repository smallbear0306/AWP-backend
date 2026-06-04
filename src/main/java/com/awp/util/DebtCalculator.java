package com.awp.util;

import com.awp.entity.DebtInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 负债分期计算。年利率 annualRatePct(%)，月利率 i = 年利率/100/12。
 * 还款方式：0 等额本息 / 1 等额本金 / 2 付息后一次性还本 / 3 一次性还本息(简单利息)。
 */
public final class DebtCalculator {

    private DebtCalculator() {
    }

    public static List<DebtInstallment> generate(double principal, double annualRatePct,
                                                 int months, int method, LocalDate firstDue) {
        List<DebtInstallment> out = new ArrayList<>();
        double i = annualRatePct / 100.0 / 12.0;
        int n = Math.max(1, months);
        LocalDate base = firstDue != null ? firstDue : LocalDate.now();

        if (method == 3) {
            // 一次性还本息：简单利息 = 本金 × 年利率 × 期限/12，到期一次还清
            double interest = principal * (annualRatePct / 100.0) * (n / 12.0);
            out.add(make(1, base, principal, interest));
            return out;
        }

        if (method == 2) {
            // 付息后一次性还本：每期付息 P*i，最后一期连本带息
            for (int k = 1; k <= n; k++) {
                double interest = principal * i;
                double ppal = (k == n) ? principal : 0.0;
                out.add(make(k, base.plusMonths(k - 1), ppal, interest));
            }
            return out;
        }

        if (method == 1) {
            // 等额本金：每期本金固定 P/n，利息按剩余本金
            double basePpal = principal / n;
            double bal = principal;
            for (int k = 1; k <= n; k++) {
                double interest = bal * i;
                double ppal = (k == n) ? bal : basePpal;
                out.add(make(k, base.plusMonths(k - 1), ppal, interest));
                bal -= ppal;
            }
            return out;
        }

        // method 0 等额本息
        double bal = principal;
        double m;
        if (i == 0) {
            m = principal / n;
        } else {
            double pow = Math.pow(1 + i, n);
            m = principal * i * pow / (pow - 1);
        }
        for (int k = 1; k <= n; k++) {
            double interest = bal * i;
            double ppal = (k == n) ? bal : (m - interest);
            out.add(make(k, base.plusMonths(k - 1), ppal, interest));
            bal -= ppal;
        }
        return out;
    }

    private static DebtInstallment make(int period, LocalDate due, double principal, double interest) {
        DebtInstallment d = new DebtInstallment();
        d.setPeriod(period);
        d.setDueDate(due);
        BigDecimal p = round(principal);
        BigDecimal it = round(interest);
        d.setPrincipal(p);
        d.setInterest(it);
        d.setAmount(p.add(it));
        d.setStatus(0);
        return d;
    }

    private static BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
