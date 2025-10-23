/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.metrica;
import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author obelm
 */
public class GraficoMetricaSwing extends JPanel {
    private final List<Double> puntos = new ArrayList<>();
    private int maxPuntos = 300;
    private String titulo = "Utilización CPU";
    private String yLabel = "0..1";
    private String xLabel = "Ciclos";

    public GraficoMetricaSwing() {
        setPreferredSize(new Dimension(640, 240));
        setBackground(Color.WHITE);
    }

    public void setMaxPuntos(int max) { this.maxPuntos = Math.max(10, max); }
    public void setTitulos(String titulo, String xLabel, String yLabel) {
        this.titulo = titulo; this.xLabel = xLabel; this.yLabel = yLabel;
    }

    public void agregarPunto(double valor01) {
        double v = Math.max(0.0, Math.min(1.0, valor01));
        if (puntos.size() >= maxPuntos) puntos.remove(0);
        puntos.add(v);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int left = 48, right = 16, top = 24, bottom = 32;
        int gw = w - left - right, gh = h - top - bottom;

        g2.setColor(Color.BLACK);
        g2.drawString(titulo, left, top - 6);
        g2.drawString(yLabel, 4, top + 10);
        g2.drawString(xLabel, w / 2 - 12, h - 6);

        g2.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= 10; i++) {
            int y = top + (int) (gh * (i / 10.0));
            g2.drawLine(left, y, left + gw, y);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(left, top, gw, gh);

        if (puntos.size() < 2) return;

        g2.setColor(new Color(66, 135, 245));
        int n = puntos.size();
        for (int i = 1; i < n; i++) {
            double v0 = puntos.get(i - 1);
            double v1 = puntos.get(i);
            int x0 = left + (int) ((i - 1) * (gw / (double) (n - 1)));
            int x1 = left + (int) ((i) * (gw / (double) (n - 1)));
            int y0 = top + gh - (int) (v0 * gh);
            int y1 = top + gh - (int) (v1 * gh);
            g2.drawLine(x0, y0, x1, y1);
        }
    }
}
