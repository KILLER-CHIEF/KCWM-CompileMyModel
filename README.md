# KCWM-CompileMyModel
Kcwm-CMM is a standalone de/obfuscator and compiler for 3d weapon models made for the Minecraft mod `KC's Weapon Mod`.

## Usage Instructions:

### Prerequisites:
- This program and its instructions are targeted toward the Microsoft Windows operating system.
- This program requires Java JDK (Java Development Kit) and 'javac' (the java code compiler) to be set in your system's PATH variable.

### Basic Usage:
1. Extract this and the rest to a folder with normal user read/write access (your desktop for example).
2. Put your model java file inside the src folder somewhere.
	There is an example model 'ModelAssaultingRifle.java' in the src/fabulousmissluna/model/ directory as an example.
3. Edit the 'KCMM Compile Weapon Model.bat' batch file (in a text editor) such that the first argument to the 'KcwmCompileMyModel.jar' is the directory where you put the model java file with respect to the 'src' folder and join each folder name with a dot '.' and include the file name without the extension.
	For example: 'java -jar KcwmCompileMyModel.jar fabulousmissluna.model.ModelAssaultingRifle'
4. Now simply execute the 'KCMM Compile Weapon Model.bat' batch (usually by double clicking on it) and if the console finishes with a success then there should now be a '.class' file along side where you put your model file. This '.class' model file is now usable ingame! If an error of some sort occured carefully read where it went wrong and see if you can figure out why it failed. If you can't figure out why just ask me.

### Extra Information:
The 'KCMM Deobfuscate src.net.bat' and 'KCMM Obfuscate src.net.bat' batch files can be used to convert the code under the 'src/net/' directory back and forth between code recognised in the decompiled vs. compiled minecraft environments. These extra files and functionalities are generally only used for tweaking the program in order for it to work with initially incompatible model files.

#

```
Author: Killer Chief
I take no responsibility for anything that goes wrong with the use of this program (not like there's anything that's gonna break your computer anyway).
Last Updated: 28.03.2016
```
