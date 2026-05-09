import { onRequest } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";
import { logger } from "firebase-functions";

admin.initializeApp();

export const login = onRequest({ cors: true }, async (req, res) => {

    if (req.method !== "POST") {
        res.status(405).json({ error: "Método não permitido. Use POST." });
        return;
    }

    const { email, senha } = req.body;

    if (!email || !senha) {
        res.status(400).json({ error: "Email e senha são obrigatórios." });
        return;
    }

    try {
        // Busca o usuário
        const userRecord = await admin.auth().getUserByEmail(email);

        logger.info(`✅ Login realizado com sucesso: ${email}`);

        // Retorna token simples (para fins acadêmicos)
        // Em produção usaria JWT real, mas aqui serve
        const token = `token_${userRecord.uid}_${Date.now()}`;

        res.status(200).json({
            token: token,
            user: {
                email: userRecord.email,
                uid: userRecord.uid,
            }
        });

    } catch (error: any) {
        logger.error("Erro no login:", error);

        if (error.code === "auth/user-not-found") {
            res.status(401).json({ error: "Email ou senha incorretos." });
        } else {
            res.status(500).json({
                error: "Erro interno ao realizar login. Tente novamente."
            });
        }
    }
});