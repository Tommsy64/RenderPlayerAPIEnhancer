// ==================================================================
// This file is part of Render Player API Enhancer.
//
// Render Player API Enhancer is free software: you can redistribute
// it and/or modify it under the terms of the GNU Lesser General
// Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later
// version.
//
// Render Player API Enhancer is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied
// warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License and the GNU General Public License along with Render
// Player API Enhancer. If not, see <http://www.gnu.org/licenses/>.
// ==================================================================

package api.player.model;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.apache.logging.log4j.*;

public class ModelPlayerAPIEnhancerClassVisitor extends ClassVisitor
{
	public static byte[] transform(byte[] bytes, boolean isObfuscated)
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			ClassReader cr = new ClassReader(in);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ModelPlayerAPIEnhancerClassVisitor p = new ModelPlayerAPIEnhancerClassVisitor(cw, isObfuscated, bytes);

			cr.accept(p, 0);

			byte[] result = bytes;
			if (p.enhancableClassName != null)
				result = cw.toByteArray();

			in.close();
			return result;
		}
		catch(IOException ioe)
		{
			throw new RuntimeException(ioe);
		}
	}

	public ModelPlayerAPIEnhancerClassVisitor(ClassVisitor classVisitor, boolean isObfuscated, byte[] bytes)
	{
		super(262144, classVisitor);
		this.isObfuscated = isObfuscated;
		this.bytes = bytes;
	}

	private final boolean isObfuscated;
	private String enhancableClassName;
	private final byte[] bytes;
	private Set<String> superCallingMethods;
	private String originalSuperClassName;
	private String enhancerSuperClassName;

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		if((isObfuscated && superName.equals("bpx")) || superName.equals("net/minecraft/client/model/ModelBiped"))
		{
			enhancableClassName = name.replace('/', '.');
			originalSuperClassName = superName;
			enhancerSuperClassName = superName = superName.equals("bpx") ? "api/player/model/ModelPlayerArmor" : "api/player/model/ModelPlayerArmor";
			info("enhances class '%s' to extend class '%s' instead of class '%s'", enhancableClassName, enhancerSuperClassName.replace('/', '.'), originalSuperClassName.replace('/', '.'));
			superCallingMethods = ModelPlayerAPIEnhancerClassScanner.scan(bytes);
		}
		else
			info("leaves class '%s' untouched because it extends the unrelated class '%s'", name.replace('/', '.'), superName.replace('/', '.'));

		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if(enhancableClassName != null && !name.equals("<cinit>") && !name.equals("<clinit>"))
		{
			if(name.equals("<init>"))
			{
				info("modifies constructor '%s%s'", enhancableClassName, desc);
				return new ModelPlayerAPIEnhancerConstructorVisitor(super.visitMethod(access, name, desc, signature, exceptions), isObfuscated);
			}
			else if((access & Opcodes.ACC_STATIC) == 0)
			{
				return new ModelPlayerAPIEnhancerMethodVisitor(super.visitMethod(access, getMethodName(name, desc), desc, signature, exceptions), isObfuscated, enhancableClassName, originalSuperClassName, enhancerSuperClassName, superCallingMethods, name, desc);
			}
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	private String getMethodName(String name, String desc)
	{
		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Lvo;)Lbrs;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetArmForSide' because it actually is 'getArmForSide' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetArmForSide";
				}
			}
			else if(name.equals("func_187074_a") && desc.equals("(Lnet/minecraft/util/EnumHandSide;)Lnet/minecraft/client/model/ModelRenderer;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetArmForSide' because it actually is 'getArmForSide' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetArmForSide";
				}
			}
		}
		else if(name.equals("getArmForSide") && desc.equals("(Lnet/minecraft/util/EnumHandSide;)Lnet/minecraft/client/model/ModelRenderer;"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localGetArmForSide' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localGetArmForSide";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Lvg;)Lvo;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetMainHand' because it actually is 'getMainHand' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetMainHand";
				}
			}
			else if(name.equals("func_187072_a") && desc.equals("(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/EnumHandSide;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetMainHand' because it actually is 'getMainHand' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetMainHand";
				}
			}
		}
		else if(name.equals("getMainHand") && desc.equals("(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/EnumHandSide;"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localGetMainHand' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localGetMainHand";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Ljava/util/Random;)Lbrs;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetRandomModelBox' because it actually is 'getRandomModelBox' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetRandomModelBox";
				}
			}
			else if(name.equals("func_85181_a") && desc.equals("(Ljava/util/Random;)Lnet/minecraft/client/model/ModelRenderer;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetRandomModelBox' because it actually is 'getRandomModelBox' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetRandomModelBox";
				}
			}
		}
		else if(name.equals("getRandomModelBox") && desc.equals("(Ljava/util/Random;)Lnet/minecraft/client/model/ModelRenderer;"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localGetRandomModelBox' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localGetRandomModelBox";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Ljava/lang/String;)Lbrt;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetTextureOffset' because it actually is 'getTextureOffset' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetTextureOffset";
				}
			}
			else if(name.equals("func_78084_a") && desc.equals("(Ljava/lang/String;)Lnet/minecraft/client/model/TextureOffset;"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localGetTextureOffset' because it actually is 'getTextureOffset' and doesn't call its super method", enhancableClassName, name, desc);
					return "localGetTextureOffset";
				}
			}
		}
		else if(name.equals("getTextureOffset") && desc.equals("(Ljava/lang/String;)Lnet/minecraft/client/model/TextureOffset;"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localGetTextureOffset' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localGetTextureOffset";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(FLvo;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localPostRenderArm' because it actually is 'postRenderArm' and doesn't call its super method", enhancableClassName, name, desc);
					return "localPostRenderArm";
				}
			}
			else if(name.equals("func_187073_a") && desc.equals("(FLnet/minecraft/util/EnumHandSide;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localPostRenderArm' because it actually is 'postRenderArm' and doesn't call its super method", enhancableClassName, name, desc);
					return "localPostRenderArm";
				}
			}
		}
		else if(name.equals("postRenderArm") && desc.equals("(FLnet/minecraft/util/EnumHandSide;)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localPostRenderArm' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localPostRenderArm";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Lvg;FFFFFF)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localRender' because it actually is 'render' and doesn't call its super method", enhancableClassName, name, desc);
					return "localRender";
				}
			}
			else if(name.equals("func_78088_a") && desc.equals("(Lnet/minecraft/entity/Entity;FFFFFF)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localRender' because it actually is 'render' and doesn't call its super method", enhancableClassName, name, desc);
					return "localRender";
				}
			}
		}
		else if(name.equals("render") && desc.equals("(Lnet/minecraft/entity/Entity;FFFFFF)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localRender' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localRender";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Lvp;FFF)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetLivingAnimations' because it actually is 'setLivingAnimations' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetLivingAnimations";
				}
			}
			else if(name.equals("func_78086_a") && desc.equals("(Lnet/minecraft/entity/EntityLivingBase;FFF)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetLivingAnimations' because it actually is 'setLivingAnimations' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetLivingAnimations";
				}
			}
		}
		else if(name.equals("setLivingAnimations") && desc.equals("(Lnet/minecraft/entity/EntityLivingBase;FFF)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localSetLivingAnimations' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localSetLivingAnimations";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Lbqf;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetModelAttributes' because it actually is 'setModelAttributes' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetModelAttributes";
				}
			}
			else if(name.equals("func_178686_a") && desc.equals("(Lnet/minecraft/client/model/ModelBase;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetModelAttributes' because it actually is 'setModelAttributes' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetModelAttributes";
				}
			}
		}
		else if(name.equals("setModelAttributes") && desc.equals("(Lnet/minecraft/client/model/ModelBase;)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localSetModelAttributes' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localSetModelAttributes";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(FFFFFFLvg;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetRotationAngles' because it actually is 'setRotationAngles' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetRotationAngles";
				}
			}
			else if(name.equals("func_78087_a") && desc.equals("(FFFFFFLnet/minecraft/entity/Entity;)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetRotationAngles' because it actually is 'setRotationAngles' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetRotationAngles";
				}
			}
		}
		else if(name.equals("setRotationAngles") && desc.equals("(FFFFFFLnet/minecraft/entity/Entity;)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localSetRotationAngles' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localSetRotationAngles";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Ljava/lang/String;II)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetTextureOffset' because it actually is 'setTextureOffset' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetTextureOffset";
				}
			}
			else if(name.equals("func_78085_a") && desc.equals("(Ljava/lang/String;II)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetTextureOffset' because it actually is 'setTextureOffset' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetTextureOffset";
				}
			}
		}
		else if(name.equals("setTextureOffset") && desc.equals("(Ljava/lang/String;II)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localSetTextureOffset' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localSetTextureOffset";
			}
		}

		if(isObfuscated)
		{
			if(name.equals("a") && desc.equals("(Z)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method ", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetVisible' because it actually is 'setVisible' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetVisible";
				}
			}
			else if(name.equals("func_178719_a") && desc.equals("(Z)V"))
			{
				if(superCallingMethods.contains(name + "___" + desc))
				{
					info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
					return name;
				}
				else
				{
					info("renames method '%s.%s%s' to 'localSetVisible' because it actually is 'setVisible' and doesn't call its super method", enhancableClassName, name, desc);
					return "localSetVisible";
				}
			}
		}
		else if(name.equals("setVisible") && desc.equals("(Z)V"))
		{
			if(superCallingMethods.contains(name + "___" + desc))
			{
				info("leaves method '%s.%s%s' untouched because it calls its super method", enhancableClassName, name, desc);
				return name;
			}
			else
			{
				info("renames method '%s.%s%s' to 'localSetVisible' because it doesn't call its super method", enhancableClassName, name, desc);
				return "localSetVisible";
			}
		}

		return name;
	}

	public static void info(String format, Object... args)
	{
		log(Level.INFO, "Render Player API Enhancer " + format, args);
	}

	public static void log(Level level, String format, Object... args)
	{
		LogManager.getLogger("RenderPlayerAPIEnhancer").log(level, String.format(format, args));
	}
}
