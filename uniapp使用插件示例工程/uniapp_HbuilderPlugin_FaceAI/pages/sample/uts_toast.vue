<template>
	<view>
		<button @click="showUTSToast">点击按钮会在底部显示一条toast</button>
	</view>
</template>

<script>
	import {
		showToast
	} from "@/uni_modules/uts-toast"
	export default {
		data() {
			return {
				title: 'Hello'
			}
		},
		onLoad() {

		},
		methods: {
			showUTSToast() {
				showToast({
					message: "这是一条显示在底部的toast"
				})
			}
		}
	}
</script>

<style>
</style>