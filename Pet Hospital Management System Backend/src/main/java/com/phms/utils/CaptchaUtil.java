package com.phms.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码工具类
 * 生成4位数字和英文字母组合的验证码图片
 */
public class CaptchaUtil {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    
    // 验证码字符集：数字0-9 + 大写字母A-Z + 小写字母a-z
    private static final String CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    private static final Random random = new Random();
    
    /**
     * 生成验证码字符串和图片
     * 
     * @return CaptchaResult 包含验证码字符串和图片
     */
    public static CaptchaResult generateCaptcha() {
        // 生成4位随机验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        String codeStr = code.toString();
        
        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 设置背景色（浅色随机背景）
        g.setColor(getRandomLightColor());
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        drawInterferenceLines(g);
        
        // 绘制验证码字符
        drawCode(g, codeStr);
        
        // 绘制干扰点
        drawInterferencePoints(g);
        
        g.dispose();
        
        return new CaptchaResult(codeStr, image);
    }
    
    /**
     * 绘制验证码字符
     */
    private static void drawCode(Graphics2D g, String code) {
        int charWidth = WIDTH / (CODE_LENGTH + 1);
        int x = charWidth / 2;
        int y = HEIGHT / 2 + 8;
        
        for (int i = 0; i < code.length(); i++) {
            // 随机字体大小
            int fontSize = 20 + random.nextInt(8);
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g.setFont(font);
            
            // 随机颜色（深色）
            g.setColor(getRandomDarkColor());
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.3; // -15度到15度
            g.rotate(angle, x, y);
            
            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            
            // 恢复旋转
            g.rotate(-angle, x, y);
            
            x += charWidth;
        }
    }
    
    /**
     * 绘制干扰线
     */
    private static void drawInterferenceLines(Graphics2D g) {
        for (int i = 0; i < 3; i++) {
            g.setColor(getRandomColor());
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    /**
     * 绘制干扰点
     */
    private static void drawInterferencePoints(Graphics2D g) {
        for (int i = 0; i < 20; i++) {
            g.setColor(getRandomColor());
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
    }
    
    /**
     * 获取随机浅色（用于背景）
     */
    private static Color getRandomLightColor() {
        int r = 200 + random.nextInt(56); // 200-255
        int g = 200 + random.nextInt(56);
        int b = 200 + random.nextInt(56);
        return new Color(r, g, b);
    }
    
    /**
     * 获取随机深色（用于文字）
     */
    private static Color getRandomDarkColor() {
        int r = random.nextInt(100); // 0-100
        int g = random.nextInt(100);
        int b = random.nextInt(100);
        return new Color(r, g, b);
    }
    
    /**
     * 获取随机颜色
     */
    private static Color getRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
    
    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private final String code;
        private final BufferedImage image;
        
        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }
        
        public String getCode() {
            return code;
        }
        
        public BufferedImage getImage() {
            return image;
        }
    }
}

