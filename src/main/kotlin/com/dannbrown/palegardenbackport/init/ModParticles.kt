package com.dannbrown.palegardenbackport.init

import com.dannbrown.palegardenbackport.ModContent
import com.dannbrown.palegardenbackport.content.particle.PaleOakParticle
import net.minecraft.core.particles.SimpleParticleType
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

object ModParticles {
  val PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ModContent.MOD_ID)

  val PALE_OAK_PARTICLE = PARTICLE_TYPES.register("pale_oak_leaves") { SimpleParticleType(true) }

  fun register(bus: IEventBus) {
    PARTICLE_TYPES.register(bus)
  }

  fun registerParticleFactories(event: RegisterParticleProvidersEvent) {
    event.registerSpriteSet(PALE_OAK_PARTICLE.get()) { PaleOakParticle.Provider(it) }
  }
}