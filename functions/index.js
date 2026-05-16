const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const { defineString } = require("firebase-functions/params");

admin.initializeApp();

// Yeni params yöntemi
const gmailPassword = defineString("GMAIL_PASSWORD");

exports.sendAdminEmail = functions.firestore
  .document("admin_notifications/{notifId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();

    if (data.type !== "feedback_milestone") return null;

    const transporter = nodemailer.createTransporter({
      service: "gmail",
      auth: {
        user: "veridia103@gmail.com",
        pass: gmailPassword.value(),
      },
    });

    const mailOptions = {
      from: "Veridia Pro <veridia103@gmail.com>",
      to: "veridia103@gmail.com",
      subject: `🌿 Veridia - ${data.count} Geri Bildirim Birikti!`,
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: #1B5E20; padding: 20px; text-align: center;">
            <h1 style="color: #69F0AE; margin: 0;">VERİDİA PRO</h1>
            <p style="color: #A5D6A7; margin: 5px 0;">Model Güncelleme Bildirimi</p>
          </div>
          <div style="padding: 30px; background: #f9f9f9;">
            <h2 style="color: #1B5E20;">📊 ${data.count} Geri Bildirim Birikti!</h2>
            <p style="color: #333; font-size: 16px;">
              Kullanıcılardan yeterli geri bildirim toplandı. 
              Modeli güncelleyebilirsiniz.
            </p>
            <div style="background: #E8F5E9; padding: 15px; border-radius: 8px; margin: 20px 0;">
              <p style="margin: 0; color: #2E7D32;"><strong>📅 Tarih:</strong> ${new Date().toLocaleString("tr-TR")}</p>
              <p style="margin: 8px 0 0; color: #2E7D32;"><strong>📈 Toplam:</strong> ${data.count} geri bildirim</p>
            </div>
            <h3 style="color: #1B5E20;">Yapılacaklar:</h3>
            <ol style="color: #333; line-height: 2;">
              <li>Firebase Console → Firestore → feedback koleksiyonunu incele</li>
              <li>Colab'da fine-tuning notebook'unu çalıştır</li>
              <li>Yeni modeli Firebase ML'e yükle</li>
              <li>Uygulama otomatik güncellenir ✅</li>
            </ol>
            <div style="text-align: center; margin-top: 30px;">
              <a href="https://console.firebase.google.com" 
                 style="background: #2E7D32; color: white; padding: 12px 24px; 
                        text-decoration: none; border-radius: 8px; font-weight: bold;">
                Firebase Console'a Git
              </a>
            </div>
          </div>
          <div style="background: #1B5E20; padding: 15px; text-align: center;">
            <p style="color: #A5D6A7; margin: 0; font-size: 12px;">
              Veridia Pro - TÜBİTAK 2209-A Projesi
            </p>
          </div>
        </div>
      `,
    };

    try {
      await transporter.sendMail(mailOptions);
      console.log("✅ Admin e-postası gönderildi!");
      return null;
    } catch (error) {
      console.error("❌ E-posta gönderilemedi:", error);
      return null;
    }
  });