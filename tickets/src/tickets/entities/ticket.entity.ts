import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn, UpdateDateColumn } from 'typeorm';

@Entity('Tickets')
export class Ticket {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column()
    placa!: string;

    @Column()
    dni!: string;

    @Column({type:'uuid'})
    idEspacio!: string;

    @Column()
    nombreZona!: string;

    @Column({type:'timestamp'})
    fechhaHoraIngreso!: Date;

    @Column({type:'timestamp', nullable: true})
    fechhaHoraSalida?: Date;

    @Column({default: true})
    activo!: boolean;

    @Column({ default: false })
    esReserva!: boolean;

    @Column({ type: 'decimal', precision: 10, scale: 2, nullable: true })
    valorRecaudo?: number;

    @CreateDateColumn()
    createdAt!: Date;

    @UpdateDateColumn()
    updatedAt!: Date;



    
}
