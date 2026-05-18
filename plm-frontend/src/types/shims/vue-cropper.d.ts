// Shim for vue-cropper: bypasses library .ts source to avoid vue-tsc 2.x TS2305 on App import
declare module 'vue-cropper' {
  const VueCropper: any
  export { VueCropper }
}
