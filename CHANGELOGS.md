# NexAuth Release Notes

## 🎉 NexAuth 0.0.1-beta3

### 💬 Developer Note

The project was stale for about 2 months as I was extensively testing different limbo systems to find the perfect fit for NexAuth. After thorough evaluation, I'm excited to announce that **NexLimbo**—specifically designed and optimized for NexAuth—has been successfully integrated into the project. This marks a significant milestone in providing seamless authentication experiences.

---

### 🚀 What's New

#### ✨ Minecraft Version Support
- **Added support for Minecraft 1.21.5 through 1.21.10**
- Updated Paper API to **1.21.10-R0.1-SNAPSHOT**
- Full compatibility with latest Velocity 3.4.0-SNAPSHOT
- Supports all Minecraft versions from **1.13 to 1.21.10**

#### 🔧 NexLimbo Integration
- **Integrated NexLimbo** as the official limbo system for NexAuth
- Replaced NanoLimboPlugin dependency with custom NexLimbo implementation
- NexLimbo is specifically designed and optimized for NexAuth's authentication flow
- Enhanced limbo server performance and stability
- Updated all plugin references and dependencies

### 🛠️ Technical Improvements

- **Adventure API Compatibility**: Fixed compatibility issues with Paper 1.21.10 by restricting Adventure version forcing to runtime configurations only
- **Build System**: Updated Gradle configuration for better dependency management
- **Project Structure**: Integrated NexLimbo as a local subproject for tighter integration

### 📋 Supported Platforms

- **Paper/Purpur**: 1.13 - 1.21.10
- **Velocity**: 3.4.0-SNAPSHOT
- **BungeeCord/Waterfall**: Latest
- **Java**: 21+

### 📦 Installation

1. Download `NexAuth.jar` from the assets below
2. Place it in your proxy/server `plugins` folder
3. Install **NexLimbo** for the complete authentication experience
4. Restart your server
5. Configure in `plugins/NexAuth/config.yml`

### ⚠️ Important Notes

- **Production Ready**: This release has been thoroughly tested and is ready for production use.
- **NexLimbo Required**: For the full authentication limbo experience, install NexLimbo on your proxy.
- **Backup**: Always backup your data before updating.

### 🔗 Links

- **Repository**: https://github.com/Xreatlabs/NexAuth
- **Documentation**: https://github.com/Xreatlabs/NexAuth/wiki
- **Issues**: https://github.com/Xreatlabs/NexAuth/issues
- **Support**: https://discord.gg/xreatlabs

---

**Full Changelog**: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta2...0.0.1-beta3
