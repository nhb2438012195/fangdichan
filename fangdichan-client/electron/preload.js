// Preload script for Electron security
const { contextBridge } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
    platform: process.platform
})
