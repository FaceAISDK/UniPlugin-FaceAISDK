import { ToastOptions } from "../interface";
import Toast from 'android.widget.Toast';

export function showToast(option : ToastOptions) : void {
	class MainThreadRunnable extends Runnable {
		override run() {
			Toast.makeText(UTSAndroid.getUniActivity()!, option.message, Toast.LENGTH_LONG).show()
		}
	}
	UTSAndroid.getUniActivity()?.runOnUiThread(new MainThreadRunnable())
}