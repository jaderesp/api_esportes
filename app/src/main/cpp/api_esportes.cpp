#include <jni.h>
#include <fstream>
#include <string>
#include <iostream>
#include <jni.h>
#include <string>
#include <fstream>
#include <sys/stat.h>
#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstdlib>
#include <vector>
#define LOG_TAG "NATIVE_URL"

std::string base64_decode(const std::string &in) {
    std::string out;
    std::vector<int> T(256, -1);
    for (int i = 0; i < 64; i++)
        T["ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"[i]] = i;

    int val = 0, valb = -8;
    for (unsigned char c : in) {
        if (T[c] == -1) break;
        val = (val << 6) + T[c];
        valb += 6;
        if (valb >= 0) {
            out.push_back(char((val >> valb) & 0xFF));
            valb -= 8;
        }
    }
    return out;
}



extern "C"
JNIEXPORT jstring JNICALL
Java_com_diegodev_apidesportes_jogos_callback_na_ae(JNIEnv *env, jobject thiz) {
    std::string parte1 = "d0c0dffd2fd08b06e";
    std::string parte2 = "17074a524017331d6";
    std::string parte3 = "04b893bffdd3d995d";
    std::string parte4 = "33b9f6ceee7a2b46b0";
    std::string parte5 = "b6e061c13adc8e0e26f237b97ba";
    std::string resultado = parte1 + parte2 + parte3 + parte4 + parte5;
    return env->NewStringUTF(resultado.c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_diegodev_apidesportes_jogos_callback_na_verificarUrlNativa(JNIEnv *env, jobject thiz, jstring urlJava) {
    const char* urlC = env->GetStringUTFChars(urlJava, nullptr);
    std::string urlStr(urlC);
    std::string parte1 = "YXBpLm";
    std::string parte2 = "Z1dGVi";
    std::string parte3 = "b2xzLmNvbS";
    std::string parte4 = "5icg==";
    std::string base64Completo = parte1 + parte2 + parte3 + parte4;

    std::string palavraEsperada = base64_decode(base64Completo);
    if (urlStr.find(palavraEsperada) != std::string::npos) {
        env->ReleaseStringUTFChars(urlJava, urlC);
        return JNI_TRUE;
    } else {
        env->ReleaseStringUTFChars(urlJava, urlC);
        exit(0);  // Encerra imediatamente
        return JNI_FALSE; // Só pra segurança
    }
}
